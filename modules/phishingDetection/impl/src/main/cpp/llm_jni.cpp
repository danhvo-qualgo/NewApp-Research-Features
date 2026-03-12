#include <jni.h>
#include <android/log.h>
#include <string>
#include <sstream>
#include <streambuf>
#include <stdexcept>

// MNN LLM C++ API — headers sourced from MNN repo:
//   include/          → src/main/cpp/include/
//   transformers/llm/engine/include/ → src/main/cpp/llm_include/
#include "llm/llm.hpp"

#define LOG_TAG "PhishingLlmJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

using namespace MNN::Transformer;

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

// Build a jstring from a raw UTF-8 std::string via String(byte[], "UTF-8").
// NewStringUTF requires Modified-UTF-8 and aborts on standard multi-byte
// sequences — this helper avoids that by going through byte[].
static jstring utf8ToJString(JNIEnv* env, const std::string& s) {
    if (s.empty()) return nullptr;
    jsize len = static_cast<jsize>(s.size());
    jbyteArray bytes = env->NewByteArray(len);
    if (!bytes) return nullptr;
    env->SetByteArrayRegion(bytes, 0, len,
        reinterpret_cast<const jbyte*>(s.data()));
    jclass    stringClass = env->FindClass("java/lang/String");
    jmethodID ctor        = env->GetMethodID(stringClass, "<init>",
                                             "([BLjava/lang/String;)V");
    jstring   charset     = env->NewStringUTF("UTF-8");
    jstring   result      = (jstring) env->NewObject(stringClass, ctor,
                                                     bytes, charset);
    env->DeleteLocalRef(charset);
    env->DeleteLocalRef(stringClass);
    env->DeleteLocalRef(bytes);
    return result;
}

// Replace all occurrences of SentencePiece word-boundary marker ▁ (U+2581,
// UTF-8: e2 96 81) with a regular space.
static std::string replaceSentencePieceSpaces(const std::string& s) {
    const std::string sp = "\xe2\x96\x81";
    std::string out;
    out.reserve(s.size());
    for (size_t i = 0; i < s.size(); ) {
        if (i + 3 <= s.size() && s[i] == '\xe2' && s[i+1] == '\x96' && s[i+2] == '\x81') {
            out += ' ';
            i += 3;
        } else {
            out += s[i++];
        }
    }
    return out;
}

// ─────────────────────────────────────────────────────────────────────────────
// TokenStreamBuf
//
// A custom std::streambuf that receives MNN's byte-by-byte token output and
// fires onProgress(chunk) on the Kotlin listener after each complete UTF-8
// character sequence, flushing eagerly at whitespace boundaries.
//
// Key correctness invariant: we never send an incomplete multi-byte UTF-8
// sequence across the JNI boundary. takeCompletePrefix() splits the internal
// buffer into a safe-to-send prefix and a held-back incomplete suffix.
// ─────────────────────────────────────────────────────────────────────────────
class TokenStreamBuf : public std::streambuf {
public:
    TokenStreamBuf(JNIEnv* env, jobject listener,
                   jmethodID onProgressId, jmethodID onFinishId)
        : mEnv(env), mListener(listener),
          mOnProgressId(onProgressId), mOnFinishId(onFinishId),
          mStopped(false) {}

    bool stopped() const { return mStopped; }

protected:
    // Called byte-by-byte by MNN.
    int overflow(int c) override {
        if (c == EOF) return EOF;
        mBuffer += static_cast<char>(c);
        // Flush eagerly at whitespace so the UI updates word-by-word.
        // takeCompletePrefix() ensures we never split a multi-byte sequence.
        if (c == ' ' || c == '\n' || c == '\t') {
            flushChunk();
        }
        return c;
    }

    // Called by tokenStream.flush() after generation ends.
    int sync() override {
        // Strip trailing stop token if MNN wrote it to the stream before halting.
        const std::string stopToken = "<|im_end|>";
        if (mBuffer.size() >= stopToken.size() &&
            mBuffer.substr(mBuffer.size() - stopToken.size()) == stopToken) {
            mBuffer.erase(mBuffer.size() - stopToken.size());
        }
        flushChunk();
        return 0;
    }

private:
    // Returns how many total bytes a UTF-8 leading byte implies for its sequence.
    static int utf8SeqLen(unsigned char b) {
        if ((b & 0x80) == 0x00) return 1; // 0xxxxxxx — ASCII
        if ((b & 0xE0) == 0xC0) return 2; // 110xxxxx
        if ((b & 0xF0) == 0xE0) return 3; // 1110xxxx
        if ((b & 0xF8) == 0xF0) return 4; // 11110xxx
        return -1;                          // continuation or invalid
    }

    // Splits mBuffer: returns the longest prefix that ends on a complete UTF-8
    // character boundary, keeping any trailing incomplete sequence in mBuffer.
    std::string takeCompletePrefix() {
        if (mBuffer.empty()) return {};

        // Walk back from the end over continuation bytes (10xxxxxx)
        int i = static_cast<int>(mBuffer.size()) - 1;
        while (i >= 0 && (static_cast<unsigned char>(mBuffer[i]) & 0xC0) == 0x80) {
            --i;
        }
        if (i < 0) return {}; // entire buffer is continuation bytes — wait

        int needed = utf8SeqLen(static_cast<unsigned char>(mBuffer[i]));
        int have   = static_cast<int>(mBuffer.size()) - i;

        if (needed < 0 || have >= needed) {
            // Last sequence is complete: flush everything.
            std::string out = mBuffer;
            mBuffer.clear();
            return out;
        } else {
            // Last sequence is incomplete: flush up to it, keep the partial tail.
            std::string out = mBuffer.substr(0, i);
            mBuffer        = mBuffer.substr(i);
            return out;
        }
    }

    void flushChunk() {
        if (mBuffer.empty() || mStopped) return;
        std::string chunk = takeCompletePrefix();
        if (chunk.empty()) return;

        // Replace SentencePiece ▁ markers with regular spaces
        chunk = replaceSentencePieceSpaces(chunk);
        if (chunk.empty()) return;

        jstring jChunk = utf8ToJString(mEnv, chunk);
        if (!jChunk) { mStopped = true; return; }

        jboolean continueGen = mEnv->CallBooleanMethod(mListener, mOnProgressId, jChunk);
        mEnv->DeleteLocalRef(jChunk);

        if (mEnv->ExceptionCheck()) {
            mEnv->ExceptionClear();
            mStopped = true;
            return;
        }
        if (continueGen == JNI_FALSE) {
            mStopped = true;
        }
    }

    JNIEnv*     mEnv;
    jobject     mListener;
    jmethodID   mOnProgressId;
    jmethodID   mOnFinishId;
    std::string mBuffer;
    bool        mStopped;
};

// ────────────────────────────────────────────────────────────────────────────
// Package: net.qualgo.safeNest.features.phishingDetection.impl.presentation
// Class:   PhishingLlmAnalyzer
// ────────────────────────────────────────────────────────────────────────────

extern "C" {

/**
 * Create and load an LLM instance.
 * @param configPath  Absolute path to llm_config.json inside the model folder.
 * @return            Opaque native pointer cast to jlong; 0 on failure.
 */
JNIEXPORT jlong JNICALL
Java_net_qualgo_safeNest_features_phishingDetection_impl_presentation_PhishingLlmAnalyzer_nativeCreate(
        JNIEnv* env, jobject /* thiz */, jstring configPath) {
    const char* path = env->GetStringUTFChars(configPath, nullptr);
    if (!path) {
        LOGE("nativeCreate: null configPath");
        return 0L;
    }
    std::string pathStr(path);
    env->ReleaseStringUTFChars(configPath, path);

    LOGI("nativeCreate: loading model from %s", pathStr.c_str());
    Llm* llm = nullptr;

    // ── Attempt 1: OpenCL (GPU) ───────────────────────────────────────────────
    // thread_num=68 is the value recommended by MNN maintainers for OpenCL on Android.
    // If OpenCL is unavailable (libMNN not built with it, or device has no GPU driver),
    // MNN will either throw or return a broken state — we catch that and fall back below.
    try {
        llm = Llm::createLLM(pathStr);
        if (!llm) {
            LOGE("nativeCreate: Llm::createLLM returned null");
            return 0L;
        }
        llm->set_config("{\"backend_type\": \"vulkan\"}");
        llm->load();
        LOGI("nativeCreate: model loaded successfully on GPU (vulkan)");
        return reinterpret_cast<jlong>(llm);
    } catch (const std::exception& e) {
        LOGI("nativeCreate: GPU load failed (%s), retrying on CPU", e.what());
        delete llm;
        llm = nullptr;
    } catch (...) {
        LOGI("nativeCreate: GPU load failed (unknown), retrying on CPU");
        delete llm;
        llm = nullptr;
    }

    // ── Attempt 2: CPU fallback ───────────────────────────────────────────────
    try {
        llm = Llm::createLLM(pathStr);
        if (!llm) {
            LOGE("nativeCreate: Llm::createLLM returned null on CPU fallback");
            return 0L;
        }
        llm->load();
        LOGI("nativeCreate: model loaded successfully on CPU");
    } catch (const std::exception& e) {
        LOGE("nativeCreate: CPU fallback also failed: %s", e.what());
        delete llm;
        return 0L;
    } catch (...) {
        LOGE("nativeCreate: CPU fallback failed (unknown exception)");
        delete llm;
        return 0L;
    }
    return reinterpret_cast<jlong>(llm);
}

/**
 * Generate a streaming response.
 *
 * MNN writes tokens byte-by-byte into TokenStreamBuf, which fires
 * listener.onProgress(chunk) for each word-level chunk as it is generated.
 * listener.onFinish() is called once generation is complete.
 *
 * @param nativePtr  Pointer returned by nativeCreate.
 * @param prompt     Full prompt string (pre-formatted with chat template).
 * @param listener   Kotlin ProgressListener instance.
 */
JNIEXPORT void JNICALL
Java_net_qualgo_safeNest_features_phishingDetection_impl_presentation_PhishingLlmAnalyzer_nativeGenerate(
        JNIEnv* env, jobject /* thiz */, jlong nativePtr, jstring prompt, jobject listener) {
    if (nativePtr == 0L) {
        LOGE("nativeGenerate: nativePtr is 0 — model not loaded");
        return;
    }

    const char* promptChars = env->GetStringUTFChars(prompt, nullptr);
    if (!promptChars) {
        LOGE("nativeGenerate: null prompt");
        return;
    }
    std::string promptStr(promptChars);
    env->ReleaseStringUTFChars(prompt, promptChars);

    jclass listenerClass = env->GetObjectClass(listener);
    jmethodID onProgressId = env->GetMethodID(listenerClass, "onProgress",
                                               "(Ljava/lang/String;)Z");
    jmethodID onFinishId   = env->GetMethodID(listenerClass, "onFinish", "()V");
    if (!onProgressId || !onFinishId) {
        LOGE("nativeGenerate: failed to find ProgressListener methods");
        return;
    }

    auto* llm = reinterpret_cast<Llm*>(nativePtr);

    // Wire the streaming buffer: MNN writes tokens into it, it fires Kotlin
    // callbacks with complete UTF-8 chunks at whitespace boundaries.
    TokenStreamBuf streamBuf(env, listener, onProgressId, onFinishId);
    std::ostream tokenStream(&streamBuf);

    try {
        llm->response(promptStr, &tokenStream, "<|im_end|>", 512);
        tokenStream.flush(); // flush any remaining partial chunk
    } catch (const std::exception& e) {
        LOGE("nativeGenerate: exception: %s", e.what());
    } catch (...) {
        LOGE("nativeGenerate: unknown exception");
    }

    if (!env->ExceptionCheck()) {
        env->CallVoidMethod(listener, onFinishId);
    }
}

/**
 * Free the native LLM instance.
 */
JNIEXPORT void JNICALL
Java_net_qualgo_safeNest_features_phishingDetection_impl_presentation_PhishingLlmAnalyzer_nativeRelease(
        JNIEnv* /* env */, jobject /* thiz */, jlong nativePtr) {
    if (nativePtr == 0L) return;
    auto* llm = reinterpret_cast<Llm*>(nativePtr);
    LOGI("nativeRelease: deleting LLM instance");
    delete llm;
}

} // extern "C"
