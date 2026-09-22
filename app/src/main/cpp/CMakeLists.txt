cmake_minimum_required(VERSION 3.22.1)
project(pocketpilot_llm LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(FetchContent)

set(LLAMA_BUILD_COMMON ON CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)

set(LLAMA_ROOT ${CMAKE_CURRENT_LIST_DIR}/llama.cpp)
if(EXISTS "${LLAMA_ROOT}/CMakeLists.txt"
    AND EXISTS "${LLAMA_ROOT}/vendor"
    AND EXISTS "${LLAMA_ROOT}/cmake/llama-config.cmake.in"
    AND EXISTS "${LLAMA_ROOT}/cmake/llama.pc.in")
    add_subdirectory(${LLAMA_ROOT} llama-build)
else()
    FetchContent_Declare(
        llama
        GIT_REPOSITORY https://github.com/ggml-org/llama.cpp.git
        GIT_TAG 6f41ac59e0a49a00483a316a22ada6b04edd2950
        GIT_SUBMODULES_RECURSE TRUE
    )
    FetchContent_MakeAvailable(llama)
    set(LLAMA_ROOT ${llama_SOURCE_DIR})
endif()

set(LLAMA_ANDROID_LOGGING_HEADER
    "${LLAMA_ROOT}/examples/llama.android/lib/src/main/cpp/logging.h")
if(EXISTS "${LLAMA_ANDROID_LOGGING_HEADER}")
    file(READ "${LLAMA_ANDROID_LOGGING_HEADER}" LLAMA_LOGGING_CONTENT)
    string(REPLACE
        [=[static inline int ai_should_log(int prio) {
    return __android_log_is_loggable(prio, LOG_TAG, LOG_MIN_LEVEL);
}]=]
        [=[static inline int ai_should_log(int prio) {
#if defined(__ANDROID_API__) && __ANDROID_API__ >= 30
    return __android_log_is_loggable(prio, LOG_TAG, LOG_MIN_LEVEL);
#else
    (void) prio;
    return 1;
#endif
}]=]
        LLAMA_LOGGING_CONTENT "${LLAMA_LOGGING_CONTENT}")
    file(WRITE "${LLAMA_ANDROID_LOGGING_HEADER}" "${LLAMA_LOGGING_CONTENT}")
endif()

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
