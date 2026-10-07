#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <cstring>

#include "llama.h"

static llama_model* g_model = nullptr;
static llama_context* g_context = nullptr;

static jstring throwIllegalState(JNIEnv* env, const char* message) {
    jclass exceptionClass = env->FindClass("java/lang/IllegalStateException");
    if (exceptionClass != nullptr) {
        env->ThrowNew(exceptionClass, message);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_vidyanova_ai_ai_inference_LocalModelEngine_nativeLoadModel(
        JNIEnv* env,
        jobject,
        jstring modelPath) {

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    if (path == nullptr) {
        return JNI_FALSE;
    }

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

    g_model = llama_model_load_from_file(path, model_params);

    env->ReleaseStringUTFChars(modelPath, path);

    if (g_model == nullptr) {
        return JNI_FALSE;
    }

    llama_context_params context_params = llama_context_default_params();

    context_params.n_ctx = 2048;
    context_params.n_batch = 256;
    context_params.n_threads = 4;
    context_params.n_threads_batch = 4;

    g_context = llama_init_from_model(g_model, context_params);

    if (g_context == nullptr) {
        llama_model_free(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }

    return JNI_TRUE;
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

    const char* promptText =
            env->GetStringUTFChars(prompt, nullptr);
    if (promptText == nullptr) {
        return nullptr;
    }

    const llama_vocab* vocab =
            llama_model_get_vocab(g_model);

    const int n_prompt = -llama_tokenize(
            vocab,
            promptText,
            static_cast<int32_t>(strlen(promptText)),
            nullptr,
            0,
            true,
            true
    );

    if (n_prompt <= 0) {
        env->ReleaseStringUTFChars(prompt, promptText);
        return throwIllegalState(env, "The prompt could not be tokenized.");
    }

    std::vector<llama_token> tokens(n_prompt);

    const int token_count = llama_tokenize(
            vocab,
            promptText,
            static_cast<int32_t>(strlen(promptText)),
            tokens.data(),
            n_prompt,
            true,
            true
    );

    if (token_count < 0) {
        env->ReleaseStringUTFChars(prompt, promptText);
        return throwIllegalState(env, "Prompt tokenization failed.");
    }

    tokens.resize(token_count);
    env->ReleaseStringUTFChars(prompt, promptText);

    llama_memory_clear(
            llama_get_memory(g_context),
            true
    );

    if (llama_decode(
            g_context,
            llama_batch_get_one(
                    tokens.data(),
                    tokens.size()
            )
    ) != 0) {
        return throwIllegalState(env, "The model could not evaluate the prompt.");
    }

    std::string result;

    llama_sampler_chain_params sampler_params =
            llama_sampler_chain_default_params();

    llama_sampler* sampler =
            llama_sampler_chain_init(sampler_params);
    if (sampler == nullptr) {
        return throwIllegalState(env, "The model sampler could not be initialized.");
    }

    llama_sampler_chain_add(
            sampler,
            llama_sampler_init_temp(temperature)
    );

    llama_sampler_chain_add(
            sampler,
            llama_sampler_init_top_p(0.9f, 1)
    );

    llama_sampler_chain_add(
            sampler,
            llama_sampler_init_dist(1234)
    );

    const int max_new_tokens =
            static_cast<int>(maxTokens);

    for (int i = 0; i < max_new_tokens; ++i) {

        llama_token token =
                llama_sampler_sample(
                        sampler,
                        g_context,
                        -1
                );

        if (llama_vocab_is_eog(vocab, token)) {
            break;
        }

        char buffer[8192];

        int length = llama_token_to_piece(
                vocab,
                token,
                buffer,
                sizeof(buffer),
                0,
                true
        );

        if (length > 0) {
            result.append(buffer, length);
        }

        if (llama_decode(
                g_context,
                llama_batch_get_one(&token, 1)
        ) != 0) {
            llama_sampler_free(sampler);
            return throwIllegalState(env, "The model failed while generating a response.");
        }
    }

    llama_sampler_free(sampler);

    return env->NewStringUTF(result.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_vidyanova_ai_ai_inference_LocalModelEngine_nativeReleaseModel(
        JNIEnv*,
        jobject) {

    if (g_context != nullptr) {
        llama_free(g_context);
        g_context = nullptr;
    }

    if (g_model != nullptr) {
        llama_model_free(g_model);
        g_model = nullptr;
    }

    llama_backend_free();
}