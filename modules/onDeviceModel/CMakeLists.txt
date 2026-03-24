# CMakeLists.txt — Unified native build for Gate 1 + Gate 2 (MNN LLM)
#
# Builds two shared libraries:
#   1. libgate1_features.so  — C feature extraction + JNI bridge
#   2. libmnnllmphishing.so  — MNN LLM inference for Gate 2
#
# Usage in build.gradle.kts:
#   android {
#       externalNativeBuild {
#           cmake {
#               path = file("CMakeLists.txt")
#           }
#       }
#   }

cmake_minimum_required(VERSION 3.22.1)
project("urlanalyzer_native")

# ─── Gate 1: C feature extraction ─────────────────────────────────────────────

add_library(gate1_features SHARED
    native/src/gate1_jni.c
    native/src/gate1_features.c
)

target_include_directories(gate1_features PRIVATE
    native/include
)

target_link_libraries(gate1_features log m)
target_compile_options(gate1_features PRIVATE -Wall -Wextra -O2)

# Required for 16 KB page-size compliance on Android 15+
target_link_options(gate1_features PRIVATE "-Wl,-z,max-page-size=16384")

# ─── Gate 2: MNN LLM inference ────────────────────────────────────────────────
# Note: This section is commented out as it references the existing MNN build
# from your scamAnalyzer module. If you need to build it here, uncomment and
# adjust the paths to point to your existing MNN native code.

# add_library(mnnllmphishing SHARED cpp/llm_jni.cpp)
# 
# # MNN core headers
# include_directories(${CMAKE_SOURCE_DIR}/cpp/include)
# # MNN LLM engine headers
# include_directories(${CMAKE_SOURCE_DIR}/cpp/llm_include)
# 
# # Pre-built libMNN.so
# add_library(MNN SHARED IMPORTED)
# set_target_properties(MNN PROPERTIES
#     IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/cpp/jniLibs/${ANDROID_ABI}/libMNN.so)
# 
# target_link_libraries(mnnllmphishing MNN android log)
# 
# # Required for 16 KB page-size compliance on Android 15+
# target_link_options(mnnllmphishing PRIVATE "-Wl,-z,max-page-size=16384")
