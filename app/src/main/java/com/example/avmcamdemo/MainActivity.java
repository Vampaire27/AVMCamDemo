package com.example.avmcamdemo;


import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.autochips.avm.AVMCam;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.GPUImageBaseParamFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageCameraInputFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;
import jp.co.cyberagent.android.gpuimage.Rotation;
import jp.co.cyberagent.android.gpuimage.TextureRotationUtil;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CameraDemo";

    private VideoRecord mRecorder = null;
    private SurfaceView mSurfaceView = null;
    private GLSurfaceView mGlSurfaceView = null;

    private GPUImageFilterGroup mFilterGroup = null;
    private GPUImageCameraInputFilter mCameraInput = null;
    private GPUImageBaseParamFilter mBaseFilter = null;
    private GPUImageGrayscaleFilter mGrayFilter = null;
    private GPUImageSketchFilter mSketchFilter = null;
    private FloatBuffer mVerCoordArray = null;
    private FloatBuffer mTexCoordArray = null;

    private int[] mOutModes = null;
    private Surface[] mOutSurfaces = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        mOutModes = new int[]{AVMCam.AVM_CAM_OUTPUT_MODE_NORMAL, AVMCam.AVM_CAM_OUTPUT_MODE_ALL_IN_ONE, AVMCam.AVM_CAM_OUTPUT_MODE_ALL_IN_ONE};
        mOutSurfaces = new Surface[]{null, null, null, null, null, null};

        AVMCam.open();
        AVMCam.enableChannel(new boolean[]{true, true, true, true});
        AVMCam.start();

        mSurfaceView = this.findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated() surface:");
                mOutSurfaces[0] = holder.getSurface();
                AVMCam.setSurface(mOutModes, mOutSurfaces);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged() size " + width + " x " + height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed()");
                mOutSurfaces[0] = null;
                AVMCam.setSurface(mOutModes, mOutSurfaces);
            }
        });

        mCameraInput = new GPUImageCameraInputFilter();
        mBaseFilter = new GPUImageBaseParamFilter();
        mGrayFilter = new GPUImageGrayscaleFilter();
        mSketchFilter = new GPUImageSketchFilter();

        mBaseFilter.setSaturation(10f);
        //mBaseFilter.setContrast(10f);

        mFilterGroup = new GPUImageFilterGroup();
        mFilterGroup.addFilter(mCameraInput);
        mFilterGroup.addFilter(mBaseFilter);
        //mFilterGroup.addFilter(mGrayFilter);
        //mFilterGroup.addFilter(mSketchFilter);

        mVerCoordArray = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexCoordArray = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        mGlSurfaceView = (GLSurfaceView) this.findViewById(R.id.glSurfaceView);
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            private int mFrameCount = 0;
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                Log.d(TAG, "onSurfaceCreated()");

                mFilterGroup.init();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                Log.d(TAG, "onSurfaceChanged()");

                mFilterGroup.onOutputSizeChanged(width, height);
                GLES20.glViewport(0, 0, width, height);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

                SurfaceTexture renderInputTex = mCameraInput.getInputSurfaceTexture();
                renderInputTex.setDefaultBufferSize(width, height);
                renderInputTex.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        Log.d(TAG, "onFrameAvailable");
                    }
                });
                mOutSurfaces[4] = new Surface(renderInputTex);
                AVMCam.setSurface(mOutModes, mOutSurfaces);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                if (mFrameCount++ % 2 == 1) {
                    //return;
                }
                Log.d(TAG, "onDrawFrame()");
                mCameraInput.getInputSurfaceTexture().updateTexImage();
                //long timestamp_ns = mCameraInput.getInputSurfaceTexture().getTimestamp();

                mVerCoordArray.position(0);
                mTexCoordArray.position(0);
                mVerCoordArray.put(GPUImageFilter.VERTEX_CUBE_NORMAL).position(0);
                mTexCoordArray.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, false)).position(0);
                mFilterGroup.onDraw(OpenGlUtils.NO_TEXTURE, mVerCoordArray, mTexCoordArray);
            }
        });

        mRecorder = new VideoRecord();
        mOutSurfaces[5] = mRecorder.prepareRecord(1920, 1080, 30, 4*1024*1024);
        if (!mRecorder.startRecord("/sdcard/avmcam.ts")) {
            mOutSurfaces[5] = null;
        }

        AVMCam.setSurface(mOutModes, mOutSurfaces);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        AVMCam.setSurface(null, null);
        AVMCam.stop();
        AVMCam.close();
        mRecorder.stopRecord();
        mRecorder.releaseRecord();
    }
}
