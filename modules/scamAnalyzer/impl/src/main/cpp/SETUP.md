# MNN Header Directories — Manual Setup Required

## `include/`
Copy the contents of `MNN/include/` from the MNN source tree here.
Minimum required files:
- `MNN/include/MNN/MNNDefine.h`  (and its siblings)

## `llm_include/`
Copy the contents of `MNN/transformers/llm/engine/include/` here.
Minimum required file:
- `llm/llm.hpp`  (+ any headers it includes transitively)

## `../jniLibs/arm64-v8a/`
Drop `libMNN.so` (from the GitHub Releases zip for your target MNN version) here.
Optional: `libMNN_CL.so` for OpenCL GPU acceleration.

All of these are *not* committed to the repository because they are large binaries
or generated artefacts. They must be set up locally before building the `:phishingDetection:impl` module.
