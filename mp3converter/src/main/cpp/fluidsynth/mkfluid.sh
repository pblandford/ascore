#!/bin/sh

export PROJECT_DIR=/home/phil/ascore2/mp3converter/libs
export NDK_ROOT=/home/phil/Android/Sdk/ndk/21.4.7075529
export NDK_TOOLCHAIN=
#aarch64-linux-android-clang

$NDK_ROOT/ndk-build V=1

set -x
cp libs/x86_64/libfluidsynth.so ${PROJECT_DIR}/x86_64
cp libs/arm64-v8a/libfluidsynth.so ${PROJECT_DIR}/arm64-v8a
cp libs/armeabi-v7a/libfluidsynth.so ${PROJECT_DIR}/armeabi-v7a
cp libs/x86/libfluidsynth.so ${PROJECT_DIR}/x86
