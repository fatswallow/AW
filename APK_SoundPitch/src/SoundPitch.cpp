
#define LOG_TAG "soundpitch"
#define LOG_NDEBUG 0
#include <utils/Log.h>

#include <jni.h>
#include <JNIHelp.h>
#include <ScopedLocalRef.h>
#include <android_runtime/AndroidRuntime.h>

#include <stdio.h>
#include <string.h>
#include "TouchToStretch.h"

static RunParameters params;

static int _initSound(int samplerate, int channels, int pitch)
{
	ALOGD("_initSound(sr=%d, ch=%d, pi=%d)", samplerate, channels, pitch);
	//RunParameters params;
	memset(&params,0,sizeof(RunParameters));
	params.channel_number = channels;
	params.sample_rate = samplerate;
	params.pitchDelta = pitch;//[-60.0 60.0]
	params.tempoDelta = 0;//[-95.0 5000.0]
	params.rateDelta = 0;//[-95.0 5000.0]
	params.quick = 1;//'q','-quick'
    params.noAntiAlias = 1;//'n','-naa'
    params.goalBPM = 0;//'b','bpm=xx'
    params.detectBPM = 0;//'b,'bpm=xx'
    params.speech = 0;
	RunParameters_checkLimits(&params);
    setupSoundTouch(&params,ALL);
	return 0;
}

static int _processSound(char *in, char *out, int frames)
{
	//ALOGD("_processSound(in=%p, out=%p, frames=%d)", in, out, frames);
	return processSoundTouch((short *)in, (short *)out, frames);
}

static int _exitSound()
{
	/* nothing */
	return 0;
}

static jint
initSound(JNIEnv *env, jobject thiz, jint samplerate, jint channels, jint pitch) {
	return _initSound(samplerate, channels, pitch);
}

static jint
processSound(JNIEnv *env, jobject thiz, jbyteArray inarray, jbyteArray outarray, jint frames) {
	int inbytes = frames*2*params.channel_number;
	char *in = new char[inbytes];
	char *out = new char[inbytes*16];
	env->GetByteArrayRegion(inarray, 0, inbytes, reinterpret_cast<jbyte*>(in));
	int oframes = _processSound(in, out, frames);
	int outbytes = oframes*2*params.channel_number;
	env->SetByteArrayRegion(outarray, 0, outbytes, reinterpret_cast<jbyte*>(out));
	ALOGD("iframes=%d, oframes=%d", frames, oframes);
	delete[] in;
	delete[] out;
	return oframes;
}

static const char *classPathName = "com/softwinner/demo/SoundPitch";

static JNINativeMethod methods[] = {
  {"initSound", "(III)I", (void*)initSound },
  {"processSound", "([B[BI)I", (void*)processSound },
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        ALOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env)
{
  if (!registerNativeMethods(env, classPathName,
                 methods, sizeof(methods) / sizeof(methods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}


// ----------------------------------------------------------------------------

/*
 * This is called by the VM when the shared library is first loaded.
 */
 
typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;
    
    ALOGI("JNI_OnLoad");

    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        ALOGE("ERROR: registerNatives failed");
        goto bail;
    }
    
    result = JNI_VERSION_1_4;
    
bail:
    return result;
}