/*
 * URLAnalyzerError.kt — Error types for the URL Analyzer pipeline.
 *
 * Mirrors iOS URLAnalyzerError.swift.
 */

package com.safenest.urlanalyzer

sealed class URLAnalyzerError(message: String) : Exception(message) {
    class ConfigLoadFailed(file: String) : URLAnalyzerError("Failed to load config: $file")
    class Gate1Failed(msg: String) : URLAnalyzerError("Gate 1 failed: $msg")
    class Gate2Failed(msg: String) : URLAnalyzerError("Gate 2 failed: $msg")
    class ModelNotFound(name: String) : URLAnalyzerError("Model not found: $name")
}
