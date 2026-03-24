/*
 * PhishingLlmAnalyzer.kt — JNI bridge to MNN LLM native library.
 *
 * This class exists solely to match the JNI function names baked into
 * cpp/llm_jni.cpp (package com.safeNest.demo.features.scamAnalyzer.impl.utils).
 * LMClient delegates all native calls here.
 *
 * DO NOT rename this class or its package — the JNI symbols in the
 * pre-built libmnnllmphishing.so depend on this exact path.
 */

@file:Suppress("PackageDirectoryMismatch")

package com.safeNest.demo.features.scamAnalyzer.impl.utils

import com.safenest.urlanalyzer.gate2.ProgressListener

object PhishingLlmAnalyzer {

    init {
        System.loadLibrary("mnnllmphishing")
    }

    // No @JvmStatic — llm_jni.cpp declares instance methods (jobject thiz),
    // not static methods (jclass clz). Kotlin object methods without @JvmStatic
    // are instance methods on the singleton, which matches the JNI signatures.
    external fun nativeCreate(configPath: String): Long
    external fun nativeGenerate(nativePtr: Long, prompt: String, listener: ProgressListener)
    external fun nativeRelease(nativePtr: Long)
}
