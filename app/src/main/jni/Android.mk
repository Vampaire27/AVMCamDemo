
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libavm_cam_jni

LOCAL_SRC_FILES:= avm_cam_jni.cpp

LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += -ldl
LOCAL_LDLIBS += -landroid
LOCAL_LDLIBS += -lnativewindow

include $(BUILD_SHARED_LIBRARY)
