cmake_minimum_required(VERSION 3.14)
project(gate1_features C)

set(CMAKE_C_STANDARD 99)

# Source files
set(SOURCES src/gate1_features.c)
set(HEADERS include/gate1_features.h)

# Include directory
include_directories(include)

# Static library (for iOS framework / Android .a)
add_library(gate1_features_static STATIC ${SOURCES})
target_include_directories(gate1_features_static PUBLIC include)
set_target_properties(gate1_features_static PROPERTIES OUTPUT_NAME gate1_features)

# Shared library (for Android .so via JNI)
add_library(gate1_features_shared SHARED ${SOURCES})
target_include_directories(gate1_features_shared PUBLIC include)
set_target_properties(gate1_features_shared PROPERTIES OUTPUT_NAME gate1_features)
target_link_libraries(gate1_features_shared m)

# Compiler flags
target_compile_options(gate1_features_static PRIVATE -Wall -Wextra -O2)
target_compile_options(gate1_features_shared PRIVATE -Wall -Wextra -O2)

# Install targets
install(TARGETS gate1_features_static gate1_features_shared
        ARCHIVE DESTINATION lib
        LIBRARY DESTINATION lib)
install(FILES ${HEADERS} DESTINATION include)
