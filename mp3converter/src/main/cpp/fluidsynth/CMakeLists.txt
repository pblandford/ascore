cmake_minimum_required(VERSION 3.9)

project(fluidsynth)

set(lib_PATH ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI})
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY  ${lib_PATH})

find_package (oboe REQUIRED CONFIG)
include_directories(include)

add_compile_options(-DOBOE_SUPPORT=1 -DHAVE_PTHREAD_H -DHAVE_STDLIB_H -DHAVE_STDIO_H -DHAVE_MATH_H -DHAVE_STRING_H -DHAVE_STDARG_H -DHAVE_SYS_SOCKET_H -DHAVE_NETINET_IN_H -DHAVE_ARPA_INET_H -DHAVE_NETINET_TCP_H -DHAVE_UNISTD_H -DHAVE_ERRNO_H -DHAVE_FCNTL_H -DVERSION=1.0.9 -O3 -DWITH_MIDI=0 -g -I${OBOE_DIR}/include)

add_library(fluidsynth SHARED
        fluid_adriver.c
        fluid_alsa.c
        fluid_aufile.c
        fluid_chan.c
        fluid_chorus.c
        fluid_conv.c
        fluid_dart.c
        fluid_defsfont.c
        fluid_dsp_float.c
        fluid_event.c
        fluid_gen.c
        fluid_hash.c
        fluid_list.c
        fluid_mdriver.c
        fluid_midi.c
        fluid_mod.c
        fluid_oboe.cpp
        fluid_portaudio.c
        fluid_ramsfont.c
        fluid_rev.c
        fluid_seqbind.c
        fluid_seq.c
        fluid_settings.c
        fluid_sndmgr.c
        fluid_synth.c
        fluid_sys.c
        fluid_tuning.c
        fluid_voice.c
        )


target_link_libraries(fluidsynth log oboe::oboe)

