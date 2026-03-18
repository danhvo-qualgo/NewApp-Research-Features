cmake_minimum_required(VERSION 3.22.1)
project("mnnllmphishing")

add_library(mnnllmphishing SHARED llm_jni.cpp)

# MNN core headers: place MNN/include/ contents here
include_directories(${CMAKE_SOURCE_DIR}/include)
# MNN LLM engine headers: place transformers/llm/engine/include/ contents here
include_directories(${CMAKE_SOURCE_DIR}/llm_include)

# Pre-built libMNN.so from jniLibs (populated via manual Step 0)
add_library(MNN SHARED IMPORTED)
set_target_properties(MNN PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libMNN.so)

target_link_libraries(mnnllmphishing MNN android log)

# Required for 16 KB page-size compliance on Android 15+
target_link_options(mnnllmphishing PRIVATE "-Wl,-z,max-page-size=16384")
