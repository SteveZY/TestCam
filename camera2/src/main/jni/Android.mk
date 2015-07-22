LOCAL_PATH:= $(call my-dir)

# TinyPlanet
include $(CLEAR_VARS)

LOCAL_CPP_EXTENSION := .cc
LOCAL_LDFLAGS   := -llog -ljnigraphics
LOCAL_SDK_VERSION := 9
LOCAL_MODULE    := libjni_tinyplanet
LOCAL_SRC_FILES := tinyplanet.cc

LOCAL_CFLAGS    += -ffast-math -O3 -funroll-loops
LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)

# JpegUtil - to make the local shared lib from prebuilt lib and then it can be referred by other module
include $(CLEAR_VARS)
LOCAL_MODULE := jpeg
#LOCAL_EXPORT_C_INCLUDES := <path to stlport includes>
#$(HOST_ECHO) 
$(warning Local path is $(LOCAL_PATH))
LOCAL_SRC_FILES := $(LOCAL_PATH)/libjpeg.so

include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_CFLAGS := -std=c++11
LOCAL_NDK_STL_VARIANT := c++_static
LOCAL_LDFLAGS   := -llog -ldl -L.
#LOCAL_LDLIBS :=  -ljpeg
LOCAL_SDK_VERSION := 9
LOCAL_MODULE    := libjni_jpegutil
LOCAL_SRC_FILES := jpegutil.cpp jpegutilnative.cpp

LOCAL_C_INCLUDES += external/jpeg
#LOCAL_C_INCLUDES += external/jpeg
#include $(PREBUILT_SHARED_LIBRARY)
LOCAL_SHARED_LIBRARIES = libjpeg # refer the local jpeg defined by the previous section

LOCAL_CFLAGS    += -ffast-math -O3 -funroll-loops
LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)