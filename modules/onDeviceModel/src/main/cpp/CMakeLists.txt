cmake_minimum_required(VERSION 3.22.1)
project(safenest_native)

# Build llama.cpp from source (cloned as submodule or placed in llama.cpp/)
set(LLAMA_DIR "${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp")

# llama.cpp build options
set(LLAMA_NATIVE OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS ON CACHE BOOL "" FORCE)

# ARM64 optimizations — critical for performance on mobile
set(CMAKE_BUILD_TYPE Release CACHE STRING "" FORCE)
set(CMAKE_C_FLAGS_RELEASE "-O3 -DNDEBUG" CACHE STRING "" FORCE)
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG" CACHE STRING "" FORCE)

# ARM NEON is automatic for arm64-v8a but ensure dotprod+fp16 for newer SoCs
if(ANDROID_ABI STREQUAL "arm64-v8a")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -march=armv8.2-a+dotprod+fp16")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv8.2-a+dotprod+fp16")
endif()

if(EXISTS "${LLAMA_DIR}/CMakeLists.txt")
    add_subdirectory(${LLAMA_DIR} llama.cpp)
else()
    message(FATAL_ERROR "llama.cpp not found at ${LLAMA_DIR}. Run: git clone https://github.com/ggml-org/llama.cpp.git ${LLAMA_DIR}")
endif()

# JNI bridge
add_library(safenest_jni SHARED
    llama_jni.cpp
    gate1_jni.c
    gate1_features.c
)

target_include_directories(safenest_jni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${LLAMA_DIR}/include
    ${LLAMA_DIR}/ggml/include
)

target_link_libraries(safenest_jni
    llama
    ggml
    android
    log
)
