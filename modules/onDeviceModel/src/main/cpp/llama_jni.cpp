/*
 * llama_jni.cpp — JNI bridge for llama.cpp on Android.
 *
 * Mirrors iOS LlamaEngine.swift:
 *   - Load GGUF model
 *   - Create context with configurable n_ctx and n_gpu_layers
 *   - Tokenize + decode + sample loop
 *   - Streaming callback to Kotlin via ProgressListener interface
 *   - Temperature 0.0 (greedy sampling)
 *
 * JNI functions:
 *   nativeLoad(modelPath, nCtx, nGpuLayers) → handle (jlong)
 *   nativeGenerate(handle, prompt, maxTokens, listener) → void
 *   nativeRelease(handle) → void
 */

#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>

#include "llama.h"
#include "ggml.h"

#define TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

struct LlamaHandle {
    llama_model *model;
    llama_context *ctx;
    llama_sampler *sampler;
    int n_ctx;
};

// Helper: convert std::string to jstring safely (handles non-ASCII)
static jstring toJString(JNIEnv *env, const std::string &s) {
    if (s.empty()) return env->NewStringUTF("");
    jbyteArray bytes = env->NewByteArray((jsize)s.size());
    env->SetByteArrayRegion(bytes, 0, (jsize)s.size(),
        reinterpret_cast<const jbyte*>(s.data()));
    jclass strClass = env->FindClass("java/lang/String");
    jmethodID ctor = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jstring charset = env->NewStringUTF("UTF-8");
    jstring result = (jstring)env->NewObject(strClass, ctor, bytes, charset);
    env->DeleteLocalRef(charset);
    env->DeleteLocalRef(strClass);
    env->DeleteLocalRef(bytes);
    return result;
}

extern "C" {

/*
 * Load model and create context.
 * Returns opaque handle (cast to jlong), or 0 on failure.
 */
JNIEXPORT jlong JNICALL
Java_com_safenest_urlanalyzer_shared_LlamaEngine_nativeLoad(
    JNIEnv *env, jobject /* this */,
    jstring jModelPath, jint nCtx, jint nGpuLayers
) {
    const char *modelPath = env->GetStringUTFChars(jModelPath, nullptr);
    LOGI("Loading model: %s (n_ctx=%d, n_gpu_layers=%d)", modelPath, nCtx, nGpuLayers);

    // Init llama backend
    llama_backend_init();

    // Model params
    auto modelParams = llama_model_default_params();
    modelParams.n_gpu_layers = nGpuLayers;

    llama_model *model = llama_model_load_from_file(modelPath, modelParams);
    env->ReleaseStringUTFChars(jModelPath, modelPath);

    if (!model) {
        LOGE("Failed to load model");
        return 0;
    }

    // Context params
    auto ctxParams = llama_context_default_params();
    ctxParams.n_ctx = nCtx;
    ctxParams.n_batch = 512;
    ctxParams.n_threads = 4;

    llama_context *ctx = llama_init_from_model(model, ctxParams);
    if (!ctx) {
        LOGE("Failed to create context");
        llama_model_free(model);
        return 0;
    }

    // Greedy sampler (temp=0)
    auto sparams = llama_sampler_chain_default_params();
    llama_sampler *sampler = llama_sampler_chain_init(sparams);
    llama_sampler_chain_add(sampler, llama_sampler_init_greedy());

    auto *handle = new LlamaHandle{model, ctx, sampler, nCtx};
    LOGI("Model loaded. ctx=%d tokens", nCtx);
    return reinterpret_cast<jlong>(handle);
}

/*
 * Generate tokens from prompt.
 * Streams each token to listener.onProgress(chunk: String): Boolean.
 * Stops if onProgress returns false or max tokens reached.
 */
JNIEXPORT void JNICALL
Java_com_safenest_urlanalyzer_shared_LlamaEngine_nativeGenerate(
    JNIEnv *env, jobject /* this */,
    jlong jHandle, jstring jPrompt, jint maxTokens, jobject listener
) {
    auto *handle = reinterpret_cast<LlamaHandle*>(jHandle);
    if (!handle || !handle->ctx) {
        LOGE("Invalid handle");
        return;
    }

    const char *prompt = env->GetStringUTFChars(jPrompt, nullptr);
    std::string promptStr(prompt);
    env->ReleaseStringUTFChars(jPrompt, prompt);

    // Get listener method
    jclass listenerClass = env->GetObjectClass(listener);
    jmethodID onProgress = env->GetMethodID(listenerClass, "onProgress",
        "(Ljava/lang/String;)Z");
    jmethodID onFinish = env->GetMethodID(listenerClass, "onFinish", "()V");

    // Tokenize prompt
    const llama_vocab *vocab = llama_model_get_vocab(handle->model);
    int n_prompt_max = promptStr.size() + 256;
    std::vector<llama_token> tokens(n_prompt_max);
    int n_tokens = llama_tokenize(vocab, promptStr.c_str(), promptStr.size(),
        tokens.data(), n_prompt_max, true, true);

    if (n_tokens < 0) {
        LOGE("Tokenization failed: %d", n_tokens);
        env->CallVoidMethod(listener, onFinish);
        return;
    }
    tokens.resize(n_tokens);
    LOGI("Prompt tokenized: %d tokens", n_tokens);

    // Check context size
    if (n_tokens > handle->n_ctx - 4) {
        LOGE("Prompt too long (%d tokens) for context (%d)", n_tokens, handle->n_ctx);
        env->CallVoidMethod(listener, onFinish);
        return;
    }

    // Clear KV cache (API renamed in latest llama.cpp)
    llama_memory_clear(llama_get_memory(handle->ctx), true);

    // Decode prompt using llama_batch_get_one for simplicity
    // Process in chunks of 512 tokens
    const int batch_size = 512;
    for (int start = 0; start < n_tokens; start += batch_size) {
        int chunk = std::min(batch_size, n_tokens - start);
        llama_batch batch = llama_batch_get_one(tokens.data() + start, chunk);
        if (llama_decode(handle->ctx, batch) != 0) {
            LOGE("Prompt decode failed at offset %d", start);
            env->CallVoidMethod(listener, onFinish);
            return;
        }
    }

    // Generate tokens
    int n_decoded = 0;
    int n_pos = n_tokens;

    for (int i = 0; i < maxTokens; i++) {
        llama_token new_token = llama_sampler_sample(handle->sampler, handle->ctx, -1);
        llama_sampler_accept(handle->sampler, new_token);

        if (llama_vocab_is_eog(vocab, new_token)) {
            LOGI("EOS after %d tokens", n_decoded);
            break;
        }

        // Detokenize
        char buf[256];
        int n = llama_token_to_piece(vocab, new_token, buf, sizeof(buf), 0, true);
        if (n > 0) {
            std::string piece(buf, n);
            jstring jPiece = toJString(env, piece);
            jboolean shouldContinue = env->CallBooleanMethod(listener, onProgress, jPiece);
            env->DeleteLocalRef(jPiece);

            if (!shouldContinue) {
                LOGI("Stopped by listener after %d tokens", n_decoded);
                break;
            }
        }

        // Decode next token
        llama_batch single = llama_batch_get_one(&new_token, 1);
        if (llama_decode(handle->ctx, single) != 0) {
            LOGE("Decode failed at token %d", n_decoded);
            break;
        }

        n_decoded++;
        n_pos++;
    }

    LOGI("Generation done: %d tokens", n_decoded);
    env->CallVoidMethod(listener, onFinish);
}

/*
 * Release model and context.
 */
JNIEXPORT void JNICALL
Java_com_safenest_urlanalyzer_shared_LlamaEngine_nativeRelease(
    JNIEnv *env, jobject /* this */, jlong jHandle
) {
    auto *handle = reinterpret_cast<LlamaHandle*>(jHandle);
    if (!handle) return;

    if (handle->sampler) llama_sampler_free(handle->sampler);
    if (handle->ctx) llama_free(handle->ctx);
    if (handle->model) llama_model_free(handle->model);
    delete handle;

    llama_backend_free();
    LOGI("Model released");
}

} // extern "C"
