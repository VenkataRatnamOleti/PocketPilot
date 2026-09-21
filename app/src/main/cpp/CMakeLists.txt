cmake_minimum_required(VERSION 3.22.1)
project(pocketpilot_llm LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(LLAMA_BUILD_COMMON ON CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)

set(LLAMA_ROOT ${CMAKE_CURRENT_LIST_DIR}/llama.cpp)
add_subdirectory(${LLAMA_ROOT} llama-build)

add_library(ai-chat SHARED
    ${LLAMA_ROOT}/examples/llama.android/lib/src/main/cpp/ai_chat.cpp)

target_include_directories(ai-chat PRIVATE
    ${LLAMA_ROOT}
    ${LLAMA_ROOT}/common
    ${LLAMA_ROOT}/include
    ${LLAMA_ROOT}/ggml/include
    ${LLAMA_ROOT}/ggml/src
    ${LLAMA_ROOT}/examples/llama.android/lib/src/main/cpp)

target_link_libraries(ai-chat llama llama-common android log)
