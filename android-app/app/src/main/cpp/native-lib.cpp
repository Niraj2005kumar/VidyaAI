#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <cstring>
#include <algorithm>
#include <stdexcept>
#include <thread>
#include <chrono>
#include <android/log.h>

#include "llama.h"
#include "ggml.h"

#define LOG_TAG "VidyaNovaNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static llama_model* g_model = nullptr;
static llama_context* g_context = nullptr;
static std::vector<llama_token> g_prev_tokens;

static void custom_ggml_abort_callback(const char* error_message) {
    LOGE("GGML ABORT INTERCEPTED: %s", error_message ? error_message : "Unknown error");
    throw std::runtime_error(error_message ? error_message : "GGML Aborted");
}

static jstring throwIllegalState(JNIEnv* env, const char* message) {
    LOGE("%s", message);
    jclass exceptionClass = env->FindClass("java/lang/IllegalStateException");
    if (exceptionClass != nullptr) {
        env->ThrowNew(exceptionClass, message);
    }
    return nullptr;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM*, void*) {
    ggml_set_abort_callback(custom_ggml_abort_callback);
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_vidyanova_ai_ai_inference_LocalModelEngine_nativeLoadModel(
        JNIEnv* env,
        jobject,
        jstring modelPath,
        jint nCtx,
        jint nBatch,
        jint nThreads) {

    if (modelPath == nullptr) {
        return JNI_FALSE;
    }

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    if (path == nullptr) {
        return JNI_FALSE;
    }

    LOGI("Loading llama model from: %s", path);

    try {
        ggml_set_abort_callback(custom_ggml_abort_callback);

        if (g_context != nullptr) {
            llama_free(g_context);
            g_context = nullptr;
        }

        if (g_model != nullptr) {
            llama_model_free(g_model);
            g_model = nullptr;
        }

        g_prev_tokens.clear();
        llama_backend_init();

        llama_model_params model_params = llama_model_default_params();
        model_params.n_gpu_layers = 0; // CPU inference on Android

        g_model = llama_model_load_from_file(path, model_params);

        env->ReleaseStringUTFChars(modelPath, path);

        if (g_model == nullptr) {
            LOGE("llama_model_load_from_file returned null.");
            return JNI_FALSE;
        }

        int hw_threads = static_cast<int>(std::thread::hardware_concurrency());
        int num_threads = nThreads > 0 ? nThreads : std::min(4, std::max(2, hw_threads));
        int ctx_size = nCtx > 0 ? nCtx : 1024;
        int batch_size = nBatch > 0 ? nBatch : 256;

        llama_context_params context_params = llama_context_default_params();
        context_params.n_ctx = ctx_size;
        context_params.n_batch = batch_size;
        context_params.n_threads = num_threads;
        context_params.n_threads_batch = num_threads;

        LOGI("Initializing llama context: n_ctx=%d, n_batch=%d, threads=%d", ctx_size, batch_size, num_threads);
        g_context = llama_init_from_model(g_model, context_params);

        if (g_context == nullptr) {
            LOGE("llama_init_from_model returned null.");
            llama_model_free(g_model);
            g_model = nullptr;
            return JNI_FALSE;
        }

        LOGI("Model loaded successfully!");
        return JNI_TRUE;
    } catch (const std::exception& ex) {
        LOGE("Native load model exception caught: %s", ex.what());
        if (g_context) { llama_free(g_context); g_context = nullptr; }
        if (g_model) { llama_model_free(g_model); g_model = nullptr; }
        return JNI_FALSE;
    } catch (...) {
        LOGE("Native load model unknown exception caught.");
        if (g_context) { llama_free(g_context); g_context = nullptr; }
        if (g_model) { llama_model_free(g_model); g_model = nullptr; }
        return JNI_FALSE;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_vidyanova_ai_ai_inference_LocalModelEngine_nativeGenerateStream(
        JNIEnv* env,
        jobject thiz,
        jstring prompt,
        jint maxTokens,
        jfloat temperature,
        jobject tokenCallback) {

    if (g_model == nullptr || g_context == nullptr) {
        return throwIllegalState(env, "The local AI model is not loaded.");
    }

    if (prompt == nullptr || maxTokens <= 0) {
        return throwIllegalState(env, "Invalid prompt or token limit.");
    }

    const char* promptText = env->GetStringUTFChars(prompt, nullptr);
    if (promptText == nullptr) {
        return nullptr;
    }

    jmethodID onTokenMethod = nullptr;
    if (tokenCallback != nullptr) {
        jclass callbackClass = env->GetObjectClass(tokenCallback);
        if (callbackClass != nullptr) {
            onTokenMethod = env->GetMethodID(callbackClass, "onToken", "(Ljava/lang/String;)V");
        }
    }

    auto start_total = std::chrono::high_resolution_clock::now();

    try {
        ggml_set_abort_callback(custom_ggml_abort_callback);

        const llama_vocab* vocab = llama_model_get_vocab(g_model);
        const int32_t promptLength = static_cast<int32_t>(strlen(promptText));

        int32_t nPrompt = llama_tokenize(
                vocab,
                promptText,
                promptLength,
                nullptr,
                0,
                true,
                true
        );

        if (nPrompt < 0) {
            nPrompt = -nPrompt;
        }

        if (nPrompt <= 0) {
            env->ReleaseStringUTFChars(prompt, promptText);
            return throwIllegalState(env, "The prompt contains no valid tokens.");
        }

        std::vector<llama_token> tokens(nPrompt);

        const int32_t tokenCount = llama_tokenize(
                vocab,
                promptText,
                promptLength,
                tokens.data(),
                static_cast<int32_t>(tokens.size()),
                true,
                true
        );

        env->ReleaseStringUTFChars(prompt, promptText);

        if (tokenCount <= 0) {
            return throwIllegalState(env, "Prompt tokenization failed.");
        }

        tokens.resize(tokenCount);

        const int32_t contextSize = static_cast<int32_t>(llama_n_ctx(g_context));
        const int32_t batchLimit = static_cast<int32_t>(llama_n_batch(g_context));

        if (batchLimit <= 0 || contextSize <= 0) {
            return throwIllegalState(env, "Invalid model context configuration.");
        }

        if (tokenCount >= contextSize) {
            return throwIllegalState(env, "Prompt is too long for the model context.");
        }

        const int32_t outputLimit = std::min(
                static_cast<int32_t>(maxTokens),
                contextSize - tokenCount
        );

        // Find matching prefix in KV cache to avoid re-decoding system prompt
        size_t matching_prefix = 0;
        while (matching_prefix < g_prev_tokens.size() &&
               matching_prefix < tokens.size() &&
               g_prev_tokens[matching_prefix] == tokens[matching_prefix]) {
            matching_prefix++;
        }

        LOGI("KV Cache Retention: prev_tokens=%zu, new_tokens=%d, matching_prefix=%zu",
             g_prev_tokens.size(), tokenCount, matching_prefix);

        if (matching_prefix > 0) {
            llama_memory_seq_rm(llama_get_memory(g_context), -1, static_cast<llama_pos>(matching_prefix), -1);
        } else {
            llama_memory_clear(llama_get_memory(g_context), true);
        }

        auto start_prompt = std::chrono::high_resolution_clock::now();

        // Process only remaining prompt tokens from matching_prefix
        int32_t pos = static_cast<int32_t>(matching_prefix);
        int32_t eval_count = tokenCount - static_cast<int32_t>(matching_prefix);

        for (int32_t offset = static_cast<int32_t>(matching_prefix); offset < tokenCount;) {

            const int32_t currentBatch = std::min(
                    batchLimit,
                    tokenCount - offset
            );

            llama_batch batch = llama_batch_init(currentBatch, 0, 1);

            for (int32_t i = 0; i < currentBatch; ++i) {
                batch.token[i] = tokens[offset + i];
                batch.pos[i] = pos + i;
                batch.n_seq_id[i] = 1;
                batch.seq_id[i][0] = 0;
                batch.logits[i] = (i == currentBatch - 1);
            }
            batch.n_tokens = currentBatch;

            const int32_t decodeResult = llama_decode(g_context, batch);
            llama_batch_free(batch);

            if (decodeResult != 0) {
                return throwIllegalState(
                        env,
                        "Prompt decoding failed. Check batch and context limits."
                );
            }

            offset += currentBatch;
            pos += currentBatch;
        }

        auto end_prompt = std::chrono::high_resolution_clock::now();
        double prompt_decode_ms = std::chrono::duration<double, std::milli>(end_prompt - start_prompt).count();
        double prompt_tok_per_sec = prompt_decode_ms > 0 ? (eval_count * 1000.0) / prompt_decode_ms : 0.0;

        LOGI("[Timing ms] Prompt decode: %.2f ms (%d new tokens evaluated, %.2f tok/s)", prompt_decode_ms, eval_count, prompt_tok_per_sec);

        llama_sampler_chain_params samplerParams = llama_sampler_chain_default_params();
        llama_sampler* sampler = llama_sampler_chain_init(samplerParams);

        if (sampler == nullptr) {
            return throwIllegalState(env, "Sampler initialization failed.");
        }

        llama_sampler_chain_add(sampler, llama_sampler_init_temp(temperature));
        llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.9f, 1));
        llama_sampler_chain_add(sampler, llama_sampler_init_dist(1234));

        std::string result;
        auto start_gen = std::chrono::high_resolution_clock::now();
        int genTokenCount = 0;

        for (int32_t i = 0; i < outputLimit; ++i) {

            llama_token token = llama_sampler_sample(sampler, g_context, -1);
            llama_sampler_accept(sampler, token);

            if (llama_vocab_is_eog(vocab, token)) {
                break;
            }

            char buffer[4096];

            int32_t pieceLength = llama_token_to_piece(
                    vocab,
                    token,
                    buffer,
                    sizeof(buffer),
                    0,
                    true
            );

            std::string pieceStr;
            if (pieceLength < 0) {
                std::vector<char> largeBuffer(static_cast<size_t>(-pieceLength) + 1);

                pieceLength = llama_token_to_piece(
                        vocab,
                        token,
                        largeBuffer.data(),
                        static_cast<int32_t>(largeBuffer.size()),
                        0,
                        true
                );

                if (pieceLength < 0) {
                    llama_sampler_free(sampler);
                    return throwIllegalState(env, "Token conversion failed.");
                }

                pieceStr.assign(largeBuffer.data(), pieceLength);
            } else if (pieceLength > 0) {
                pieceStr.assign(buffer, pieceLength);
            }

            if (!pieceStr.empty()) {
                result.append(pieceStr);
                genTokenCount++;

                if (tokenCallback != nullptr && onTokenMethod != nullptr) {
                    jstring tokenJStr = env->NewStringUTF(pieceStr.c_str());
                    if (tokenJStr != nullptr) {
                        env->CallVoidMethod(tokenCallback, onTokenMethod, tokenJStr);
                        env->DeleteLocalRef(tokenJStr);
                    }
                }
            }

            if (i + 1 < outputLimit) {
                llama_batch batch = llama_batch_init(1, 0, 1);
                batch.token[0] = token;
                batch.pos[0] = pos;
                batch.n_seq_id[0] = 1;
                batch.seq_id[0][0] = 0;
                batch.logits[0] = true;
                batch.n_tokens = 1;

                const int32_t decodeResult = llama_decode(g_context, batch);
                llama_batch_free(batch);

                if (decodeResult != 0) {
                    llama_sampler_free(sampler);
                    return throwIllegalState(
                            env,
                            "Model decoding failed during generation."
                    );
                }

                pos++;
            }
        }

        llama_sampler_free(sampler);

        g_prev_tokens = tokens;

        auto end_gen = std::chrono::high_resolution_clock::now();
        double generation_ms = std::chrono::duration<double, std::milli>(end_gen - start_gen).count();
        double gen_tok_per_sec = generation_ms > 0 ? (genTokenCount * 1000.0) / generation_ms : 0.0;

        auto end_total = std::chrono::high_resolution_clock::now();
        double total_ms = std::chrono::duration<double, std::milli>(end_total - start_total).count();

        LOGI("[Timing ms] Token generation: %.2f ms (%d tokens, %.2f tok/s)", generation_ms, genTokenCount, gen_tok_per_sec);
        LOGI("[Timing ms] Total inference time: %.2f ms", total_ms);

        if (result.empty()) {
            return throwIllegalState(env, "The model generated an empty response.");
        }

        return env->NewStringUTF(result.c_str());

    } catch (const std::exception& ex) {
        LOGE("Native generate exception caught: %s", ex.what());
        return throwIllegalState(env, ex.what());
    } catch (...) {
        LOGE("Native generate unknown exception caught.");
        return throwIllegalState(env, "Unknown native exception during AI generation.");
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_vidyanova_ai_ai_inference_LocalModelEngine_nativeGenerate(
        JNIEnv* env,
        jobject thiz,
        jstring prompt,
        jint maxTokens,
        jfloat temperature) {
    return Java_com_vidyanova_ai_ai_inference_LocalModelEngine_nativeGenerateStream(
            env, thiz, prompt, maxTokens, temperature, nullptr);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_vidyanova_ai_ai_inference_LocalModelEngine_nativeReleaseModel(
        JNIEnv*,
        jobject) {

    LOGI("Releasing llama model...");

    try {
        g_prev_tokens.clear();
        if (g_context != nullptr) {
            llama_free(g_context);
            g_context = nullptr;
        }

        if (g_model != nullptr) {
            llama_model_free(g_model);
            g_model = nullptr;
        }

        llama_backend_free();
    } catch (...) {
        LOGE("Exception while releasing model.");
    }
}
