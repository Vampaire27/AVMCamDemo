package com.autochips.avm;

import android.util.Log;
import android.view.Surface;

public class AVMCam {
    private static final String TAG = AVMCam.class.getSimpleName();

    public static final int AVM_CAM_OUTPUT_MODE_NORMAL = 0; //Each channel output to a Surface
    public static final int AVM_CAM_OUTPUT_MODE_ALL_IN_ONE = 1;//All channel output to a Surface

    private AVMCam () {
        Log.d(TAG, "Enter");
        Log.d(TAG, "Leave");
    }

    static {
        System.loadLibrary("avm_cam_jni");
    }

    public static native int open ();

    public static native int enableChannel (boolean[] enable);

    public static native int setSurface (int[] outModes, Surface[] outSurfaces);

    public static native int start ();

    public static native int stop ();

    public static native int close ();
}
