cmake_minimum_required(VERSION 3.22.1)

project("vidyanova")

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
add_link_options("-Wl,-z,max-page-size=16384")

set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_CUDA OFF CACHE BOOL "" FORCE)
set(GGML_METAL OFF CACHE BOOL "" FORCE)
set(GGML_VULKAN OFF CACHE BOOL "" FORCE)
set(GGML_SYCL OFF CACHE BOOL "" FORCE)

set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)

set(LLAMA_CPP_DIR "${CMAKE_CURRENT_LIST_DIR}/../../../../llama.cpp")

add_subdirectory(
    "${LLAMA_CPP_DIR}"
    "${CMAKE_CURRENT_BINARY_DIR}/llama.cpp"
)

add_library(
    vidyanova_native
    SHARED
    native-lib.cpp
)

set_target_properties(vidyanova_native PROPERTIES LINK_FLAGS "-Wl,-z,max-page-size=16384")

if (TARGET llama)
    set_target_properties(llama PROPERTIES LINK_FLAGS "-Wl,-z,max-page-size=16384")
    target_link_options(llama PUBLIC "-Wl,-z,max-page-size=16384")
endif()

if (TARGET ggml)
    set_target_properties(ggml PROPERTIES LINK_FLAGS "-Wl,-z,max-page-size=16384")
    target_link_options(ggml PUBLIC "-Wl,-z,max-page-size=16384")
endif()

if (TARGET ggml-base)
    set_target_properties(ggml-base PROPERTIES LINK_FLAGS "-Wl,-z,max-page-size=16384")
    target_link_options(ggml-base PUBLIC "-Wl,-z,max-page-size=16384")
endif()

if (TARGET ggml-cpu)
    set_target_properties(ggml-cpu PROPERTIES LINK_FLAGS "-Wl,-z,max-page-size=16384")
    target_link_options(ggml-cpu PUBLIC "-Wl,-z,max-page-size=16384")
endif()

target_link_libraries(
    vidyanova_native
    llama
    log
    android
)
