cmake_minimum_required(VERSION 3.22.1)
project(gate1_features C)

set(NATIVE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/native)

# Gate 1 shared library for Android (loaded via JNI)
add_library(gate1_features SHARED
    ${CMAKE_CURRENT_SOURCE_DIR}/gate1_jni.c
    ${NATIVE_DIR}/src/gate1_features.c
)

target_include_directories(gate1_features PRIVATE
    ${NATIVE_DIR}/include
)

target_link_libraries(gate1_features log m)
target_compile_options(gate1_features PRIVATE -Wall -Wextra -O2)
