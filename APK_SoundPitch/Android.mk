LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := lib/libSoundTouch.a
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES:= frameworks/av/media/soundpitch/inc
LOCAL_SRC_FILES:= demo/main.c
LOCAL_MODULE := sounddemo
LOCAL_STATIC_LIBRARIES:= libSoundTouch
LOCAL_SHARED_LIBRARIES:= libcutils liblog
LOCAL_MODULE_TAGS := optional
#include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES:= frameworks/av/media/soundpitch/inc
LOCAL_SRC_FILES:= src/SoundPitch.cpp
LOCAL_MODULE := libSoundPitch
LOCAL_STATIC_LIBRARIES:= libSoundTouch
LOCAL_SHARED_LIBRARIES:= libcutils libutils
LOCAL_MODULE_TAGS := optional
LOCAL_PRELINK_MODULE := false
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_MODULE := soundpitchapi
LOCAL_SDK_VERSION := current
#include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := samples
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := SoundDemo
#LOCAL_JNI_SHARED_LIBRARIES := libSoundPitch
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_SDK_VERSION := current
include $(BUILD_PACKAGE)