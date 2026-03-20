# CMakeLists.txt — NDK build for gate1_features + JNI bridge
#
# Usage in app/build.gradle.kts:
#   android {
#       externalNativeBuild {
#           cmake {
#               path = file("src/main/cpp/CMakeLists.txt")
#           }
#       }
#   }

cmake_minimum_required(VERSION 3.18)
project(gate1_features C)

add_library(gate1_features SHARED
    src/gate1_features.c
    src/gate1_jni.c
)

target_include_directories(gate1_features PRIVATE
    include
)

# Link Android log library for debugging
find_library(log-lib log)
target_link_libraries(gate1_features ${log-lib})
