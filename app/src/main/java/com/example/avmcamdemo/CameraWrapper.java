package com.example.avmcamdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.List;

public class CameraWrapper {
    private static final String TAG = "CameraWrapper";
    private CameraManager mCamManager = null;
    private CameraDevice mCamDevice = null;
    private CaptureRequest.Builder mCamRequestBuilder = null;
    private CameraCaptureSession mCamSession = null;

    private Context mContext = null;
    private String mCamId = null;
    private List<Surface> mCamOutputs = null;
    private Handler mHandler = null;

    private CaptureRequest.Key<int[]> mRequestKeyMirror = null;
    private CaptureRequest.Key<int[]> mRequestKeyTvdYGain = null;
    private CaptureResult.Key<int[]> mResultKeySouceInfo = null;

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private int mSourceWidth = -1;
        private int mSourceHeight = -1;
        private int mCount = 0;
        private long mPrevTime = 0;
        //private long mPrevTimeStamp = 0;

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //Log.d(TAG, "CameraCaptureSession.CaptureCallback.onCaptureCompleted() request:" + request + ", result:" + result);
//            long timestamp = result.get(CaptureResult.SENSOR_TIMESTAMP);
//            if (mPrevTimeStamp == 0)
//                mPrevTimeStamp = timestamp;
//            Log.d(TAG, "timestamp: " + (timestamp - mPrevTimeStamp));
//            mPrevTimeStamp = timestamp;
            if (mResultKeySouceInfo != null) {
                int[] sourceInfo = result.get(mResultKeySouceInfo);
                if (sourceInfo != null && (mSourceWidth != sourceInfo[0] || mSourceHeight != sourceInfo[1])) {
                    Log.d(TAG, "Camera " + mCamId + " Source Info Changed: " + mSourceWidth + " x " + mSourceHeight + " ==> " + sourceInfo[0] + " x " + sourceInfo[1]);
                    mSourceWidth = sourceInfo[0];
                    mSourceHeight = sourceInfo[1];
                }
            }

            mCount ++;
            if (0 == mPrevTime) {
                mPrevTime = System.currentTimeMillis();
            }
            if (mPrevTime + 1000 <= System.currentTimeMillis()) {
                Log.d(TAG, "CameraId " + mCamId + " fps = " + mCount);
                mPrevTime += 1000;
                mCount = 0;
            }
        }
    };

    private CameraDevice.StateCallback mCamDevStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "onOpened()");
            mCamDevice = camera;
            try {
                mCamDevice.createCaptureSession(mCamOutputs, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        Log.d(TAG, "CameraCaptureSession.StateCallback.onConfigured()");
                        mCamSession = session;
                        try {
                            mCamRequestBuilder = mCamDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            for (Surface surface : mCamOutputs) {
                                mCamRequestBuilder.addTarget(surface);
                            }
                            mCamRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                            if (mRequestKeyMirror != null) {
                                mCamRequestBuilder.set(mRequestKeyMirror, new int[]{0});
                            }
                            if (mRequestKeyTvdYGain != null) {
                                mCamRequestBuilder.set(mRequestKeyTvdYGain, new int[] {6550});
                            }
                            mCamSession.setRepeatingRequest(mCamRequestBuilder.build(), mCaptureCallback, mHandler);
                            Log.d(TAG, "setRepeatingRequest() end");
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.d(TAG, "CameraCaptureSession.StateCallback.onConfigureFailed()");
                        session.close();
                        mCamSession = null;
                    }
                }, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "CameraDevice.StateCallback.onDisconnected()");
            closeCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "CameraDevice.StateCallback.onError()");
            closeCamera();
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            super.onClosed(camera);
            Log.d(TAG, "CameraDevice.StateCallback.onClosed()");
        }
    };

    public CameraWrapper(String camid, Context context, Handler handler) {
        mCamId = camid;
        mContext = context;
        mHandler = handler;
        mCamManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        Log.i(TAG, "mCamId = " + mCamId);

        CameraCharacteristics characteristics = null;
        try {
            characteristics = mCamManager.getCameraCharacteristics(mCamId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }
        int lens_facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        int sensor_orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int info_supportedHardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

        Log.d(TAG, "CamId:" + mCamId + " LensFacing:" + lens_facing + " sensor_orientation:" + sensor_orientation + " info_supportedHardwareLevel:" + info_supportedHardwareLevel);
        StreamConfigurationMap scMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        int[] formats = scMap.getOutputFormats();
        for (int fmt : formats) {
            Size[] sizes = scMap.getOutputSizes(fmt);
            for (Size size : sizes) {
                long minFrameDuration = scMap.getOutputMinFrameDuration(fmt, size);
                //Log.d(TAG, "OutputStream pixfmt:" + fmt + " size:" + size.getWidth() + "x" + size.getHeight() + " minDuration:" + minFrameDuration);
            }
        }

        List<CaptureRequest.Key<?>> requestKeys = characteristics.getAvailableCaptureRequestKeys();
        for (CaptureRequest.Key<?> k : requestKeys) {
            //Log.d(TAG, "Request key " + k);
            if (k.getName().equals("com.atc.mirror")) {
                mRequestKeyMirror = (CaptureRequest.Key<int[]>) k;
            } else if (k.getName().equals("com.atc.tvd.ygain")) {
                mRequestKeyTvdYGain = (CaptureRequest.Key<int[]>) k;
            }
        }

        List<CaptureResult.Key<?>> resultKeys = characteristics.getAvailableCaptureResultKeys();
        for (CaptureResult.Key<?> k : resultKeys) {
            //Log.d(TAG, "Result key " + k);
            if (k.getName().equals("com.atc.sourceinfo")) {
                mResultKeySouceInfo = (CaptureResult.Key<int[]>) k;
            }
        }

    }

    public boolean openCamera(List<Surface> outputs) {
        mCamOutputs = outputs;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        try {
            Log.i(TAG, "mCamId = " + mCamId);
            mCamManager.openCamera(mCamId, mCamDevStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void closeCamera()
    {
        Log.d(TAG, "stopCameraView()");
        if (null != mCamSession) {
            mCamSession.close();
            mCamSession = null;
        }
        if (null != mCamDevice) {
            mCamDevice.close();
            mCamDevice = null;
        }
    }
}
