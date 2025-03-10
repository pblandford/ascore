cmake_minimum_required(VERSION 3.4.1)

project(mp3converter)

set(lib_PATH ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI})
set(native_PATH ${CMAKE_SOURCE_DIR}/jni)
set(fluid_source_PATH ${CMAKE_SOURCE_DIR}/fluidsynth)
set(lame_source_PATH ${CMAKE_SOURCE_DIR}/lame)


file(GLOB lame_files ${lame_source_PATH}/libmp3lame/*.c)

add_subdirectory(${fluid_source_PATH} ${lib_PATH})

include_directories(${fluid_source_PATH}/include ${fluid_source_PATH}/include/fluidsynth ${fluid_source_PATH}/build/include ${lame_source_PATH}/include)


add_library(fluid-native
        SHARED
        ${native_PATH}/fluid-native.c)

add_library(lame-native
        SHARED
        ${native_PATH}/lame-native.c)

add_definitions(-DSTDC_HEADERS)
add_library(lame
        SHARED
        ${lame_files}
        )

target_link_libraries(lame-native
        lame
        android
        log)

target_link_libraries(fluid-native
        fluidsynth
        android
        log)

