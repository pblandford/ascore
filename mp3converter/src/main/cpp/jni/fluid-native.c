
//
// Created by philb on 15/03/20.
//

#include <string.h>
#include <jni.h>
#include <stdlib.h>
#include <android/log.h>

#include <fluidsynth.h>

#include <types.h>
#include <misc.h>
#include <log.h>

#include <audio.h>
#include <settings.h>
#include <synth.h>
#include <sfont.h>
#include "../fluidsynth/fluid_sfont.h"

#define FLUID_OK 0

typedef struct handle {
    fluid_settings_t *settings;
    fluid_audio_driver_t *driver;
    fluid_synth_t *synth;
    int sfont_id;
} handle_t;

static void log_function(int level, char *message, void *data) {
    __android_log_print(ANDROID_LOG_ERROR, "FLD", "%s", message);
}

JNIEXPORT jlong JNICALL
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_openFluid(JNIEnv
                                                                           *env,
                                                                           jobject thiz, jstring
                                                                           soundfontPath,
                                                                           jboolean create_driver) {
    const char *nativeString = (*env)->GetStringUTFChars(env, soundfontPath, NULL);

    __android_log_print(ANDROID_LOG_DEBUG,
                        "FLD", "Initialising fluidsynth %s", nativeString);


    fluid_set_log_function(FLUID_INFO, log_function, NULL);
    fluid_set_log_function(FLUID_ERR, log_function, NULL);
    fluid_settings_t *settings = new_fluid_settings();
    fluid_settings_setstr(settings, "audio.driver", "oboe");
    fluid_settings_setint(settings, "synth.midi-channels", 32);
    fluid_synth_t *synth = new_fluid_synth(settings);
    fluid_audio_driver_t *driver = NULL;
    if (create_driver) {
        driver = new_fluid_audio_driver(settings, synth);
    }
    int sfont_id = fluid_synth_sfload(synth, nativeString,
                                      1);
    if (sfont_id == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "FLD", "Failed initialising soundfont %s",
                            nativeString);
        return -1L;
    }
    (*env)->
            ReleaseStringUTFChars(env, soundfontPath, nativeString
    );
    __android_log_print(ANDROID_LOG_DEBUG, "FLD", "Initialised fluidsynth %s", nativeString);
    handle_t *handle = malloc(sizeof(handle_t));
    handle->settings = settings;
    handle->driver = driver;
    handle->synth = synth;
    handle->sfont_id = sfont_id;
    return (jlong) handle;
}


JNIEXPORT void JNICALL
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_closeFluid(JNIEnv
                                                                            *env,
                                                                            jobject thiz,
                                                                            jlong handle) {
    handle_t *handleStruct = (handle_t *) handle;
    if (handleStruct != NULL) {
        if (handleStruct->driver != NULL) delete_fluid_audio_driver(handleStruct->driver);
        if (handleStruct->synth != NULL) delete_fluid_synth(handleStruct->synth);
        if (handleStruct->settings != NULL) delete_fluid_settings(handleStruct->settings);
        free(handleStruct);
    }
}

JNIEXPORT jshortArray
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_getSampleData(JNIEnv
                                                                               *env,
                                                                               jclass thiz,
                                                                               jlong handle,
                                                                               jlong numShorts) {

    fluid_synth_t *synth = ((handle_t *) handle)->synth;
    short *result = (short *) calloc(1, numShorts * 2);

    fluid_synth_write_s16(synth, numShorts, result, 0, 1, result, 0, 1);

    jshortArray retArray = (*env)->NewShortArray(env, numShorts);
    (*env)->SetShortArrayRegion(env, retArray, 0, numShorts, result);
    free(result);

    return retArray;
}

JNIEXPORT void JNICALL
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_programChange(JNIEnv *env,
                                                                               jclass thiz,
                                                                               jlong handle,
                                                                               jint channel,
                                                                               jint midiId) {
    fluid_synth_t *synth = ((handle_t *) handle)->synth;


    __android_log_print(ANDROID_LOG_ERROR, "FLD", "programChange %d %d", channel, midiId);

    int ret = fluid_synth_program_change(synth, channel, midiId - 1);
    if (ret != FLUID_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "FLD",
                            "Failed changing program: channel %d program %d", channel, midiId);
    }
}


JNIEXPORT void JNICALL
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_noteOn(JNIEnv *env,
                                                                        jclass thiz,
                                                                        jlong handle,
                                                                        jint channel,
                                                                        jint midiVal,
                                                                        jint velocity) {
    fluid_synth_t *synth = ((handle_t *) handle)->synth;

    int ret = fluid_synth_noteon(synth, channel, midiVal, velocity);
    if (ret != FLUID_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "FLD", "Failed note on %d %d %d", midiVal, velocity,
                            channel);
    }
}

JNIEXPORT void JNICALL
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_noteOff(JNIEnv *env,
                                                                         jclass thiz,
                                                                         jlong handle,
                                                                         jint channel,
                                                                         jint midiVal) {
    fluid_synth_t *synth = ((handle_t *) handle)->synth;

    int ret = fluid_synth_noteoff(synth, channel, midiVal);
    if (ret != FLUID_OK) {
        //   __android_log_write(ANDROID_LOG_ERROR, "FLD", "Failed note off");
    }
}

JNIEXPORT void JNICALL
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_pedal(JNIEnv *env,
                                                                       jclass thiz,
                                                                       jlong handle,
                                                                       jint channel,
                                                                       jboolean on) {
    fluid_synth_t *synth = ((handle_t *) handle)->synth;

    int value;
    if (on) {
        value = 0x7f;
    } else {
        value = 0;
    }
    int ret = fluid_synth_cc(synth, channel, 0x40, value);
    if (ret != FLUID_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "FLD", "Failed pedal on %d %x %d %x", value, value,
                            0x50, 0x50);
    }
}

JNIEXPORT jstring JNICALL
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_getPresets(JNIEnv *env,
                                                                            jclass thiz,
                                                                            jlong handle) {
    fluid_synth_t *synth = ((handle_t *) handle)->synth;
    fluid_sfont_t *soundfont = fluid_synth_get_sfont(synth, 0);

    int buff_size = 1024;
    char *fullString = calloc(1, buff_size);
    char tmpBuf[2048];

    fluid_sfont_iteration_start(soundfont);
    fluid_preset_t preset;

    while (fluid_sfont_iteration_next(soundfont, &preset)) {
        const char *name = fluid_preset_get_name(&preset);
        int program = fluid_preset_get_num(&preset) + 1;
        const char *sfname = fluid_sfont_get_name(soundfont);
        int bank = fluid_preset_get_banknum(&preset);
        unsigned int len = strlen(name) + 3 + 3 + strlen(sfname) + 2;
        sprintf(tmpBuf, "%s,%d,%s,%d\n", name, program, sfname, bank);
        if (strlen(fullString) + len > buff_size) {
            buff_size += 1024;
            fullString = realloc(fullString, buff_size);
        }
        fullString = strcat(fullString, tmpBuf);
        __android_log_print(ANDROID_LOG_ERROR, "FLD", "preset %s %s %d %d", name, sfname, program,
                            bank);
    }
    jstring result = (*env)->NewStringUTF(env, fullString);
    free(fullString);
    return result;
}