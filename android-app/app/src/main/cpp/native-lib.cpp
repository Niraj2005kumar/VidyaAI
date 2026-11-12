#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <cstring>
#include <algorithm>
#include <stdexcept>
#include <android/log.h>

#include "llama.h"
#include "ggml.h"

#define LOG_TAG "VidyaNovaNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static llama_model* g_model = nullptr;
static llama_context* g_context = nullptr;

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
        jstring modelPath) {

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

        llama_backend_init();

        llama_model_params model_params = llama_model_default_params();
        model_params.n_gpu_layers = 0; // Use CPU inference on Android

        g_model = llama_model_load_from_file(path, model_params);

        env->ReleaseStringUTFChars(modelPath, path);

        if (g_model == nullptr) {
            LOGE("llama_model_load_from_file returned null.");
            return JNI_FALSE;
        }

        llama_context_params context_params = llama_context_default_params();
        context_params.n_ctx = 2048;
        context_params.n_batch = 512;
        context_params.n_threads = 4;
        context_params.n_threads_batch = 4;

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
Java_com_vidyanova_ai_ai_inference_LocalModelEngine_nativeGenerate(
        JNIEnv* env,
        jobject,
        jstring prompt,
        jint maxTokens,
        jfloat temperature) {

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

        llama_memory_clear(llama_get_memory(g_context), true);

        for (int32_t offset = 0; offset < tokenCount;) {

            const int32_t currentBatch = std::min(
                    batchLimit,
                    tokenCount - offset
            );

            llama_batch batch = llama_batch_get_one(
                    tokens.data() + offset,
                    currentBatch
            );

            const int32_t decodeResult = llama_decode(g_context, batch);

            if (decodeResult != 0) {
                return throwIllegalState(
                        env,
                        "Prompt decoding failed. Check batch and context limits."
                );
            }

            offset += currentBatch;
        }

        llama_sampler_chain_params samplerParams = llama_sampler_chain_default_params();
        llama_sampler* sampler = llama_sampler_chain_init(samplerParams);

        if (sampler == nullptr) {
            return throwIllegalState(env, "Sampler initialization failed.");
        }

        llama_sampler_chain_add(sampler, llama_sampler_init_temp(temperature));
        llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.9f, 1));
        llama_sampler_chain_add(sampler, llama_sampler_init_dist(1234));

        std::string result;

        for (int32_t i = 0; i < outputLimit; ++i) {

            llama_token token = llama_sampler_sample(sampler, g_context, -1);

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

                result.append(largeBuffer.data(), pieceLength);
            } else if (pieceLength > 0) {
                result.append(buffer, pieceLength);
            }

            if (i + 1 < outputLimit) {
                llama_batch batch = llama_batch_get_one(&token, 1);

                if (llama_decode(g_context, batch) != 0) {
                    llama_sampler_free(sampler);
                    return throwIllegalState(
                            env,
                            "Model decoding failed during generation."
                    );
                }
            }
        }

        llama_sampler_free(sampler);

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
JNIEXPORT void JNICALL
Java_com_vidyanova_ai_ai_inference_LocalModelEngine_nativeReleaseModel(
        JNIEnv*,
        jobject) {

    LOGI("Releasing llama model...");

    try {
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
