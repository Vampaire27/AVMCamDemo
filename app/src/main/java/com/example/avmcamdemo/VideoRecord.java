package com.example.avmcamdemo;

import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

public class VideoRecord {
    private final static String TAG = "VideoRecord";

    private MediaRecorder mRecorder = null;
    private int mWidth = 0;
    private int mHeight = 0;
    private int mFps = 0;
    private int mBitRate = 0;
    private Surface mSurface = null;

    public VideoRecord() {
        Log.d(TAG, "VideoRecord()");
    }

    public Surface prepareRecord(int width, int height, int fps, int bitrate)
    {
        Log.d(TAG, "prepareRecord() start");

        //If mRecorder not null, stop mRecorder again.
        if (null != mRecorder) {
            Log.e(TAG, "[DVRRec_startRecord]mRecorder is not null");
            return null;
        }

        mWidth = width;
        mHeight = height;
        mFps = fps;
        mBitRate = bitrate;
        mSurface = MediaCodec.createPersistentInputSurface();

        //ONLY FOR INIT Surface
        mRecorder = new MediaRecorder();
        //1. Set error&info listener
        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.e(TAG, "onError() what: " + what + ", extra: " + extra);
            }
        });
        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                Log.e(TAG, "onInfo() what: " + what + ", extra: " + extra);
            }
        });
        mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mRecorder.setInputSurface(mSurface);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
        mRecorder.setVideoSize(mWidth, mHeight);
        mRecorder.setVideoFrameRate(mFps);
        mRecorder.setVideoEncodingBitRate(mBitRate);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mRecorder.setOutputFile("/sdcard/tmp.ts");
        //prepare record
        try {
            mRecorder.prepare();
            mRecorder.release();
            mRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "prepareRecord() end, surface = " + mSurface);
        return mSurface;
    }

    public boolean startRecord(String file)
    {
        Log.d(TAG, "startRecord() start");
        boolean ret = true;

        mRecorder = new MediaRecorder();

        //1. Set error&info listener
        mRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {
                Log.e(TAG, "onError() what: " + what + ", extra: " + extra);
            }
        });
        mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                Log.e(TAG, "onInfo() what: " + what + ", extra: " + extra);
            }
        });

        mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mRecorder.setInputSurface(mSurface);

        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);

        //6. Set video record size
        mRecorder.setVideoSize(mWidth, mHeight);

        //7. Set video parameter
        mRecorder.setVideoFrameRate(mFps);
        mRecorder.setVideoEncodingBitRate(mBitRate);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mRecorder.setOutputFile(file);
        Log.d(TAG, "New file : " + file);

        //start record
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            ret = false;
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        } catch (RuntimeException e) {
            e.printStackTrace();
            ret = false;
        } finally {
            if (!ret) {
                Log.d(TAG, "MediaRecorder prepare failed! Try to stop and release mRecorder & lock camera");
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            }
        }

        Log.d(TAG, "startRecord() end, ret = " + ret);
        return ret;
    }

    public void stopRecord()
    {
        Log.d(TAG, "stopRecord() start");
        boolean flag = true;
        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                flag = false;
            } catch (RuntimeException e) {
                e.printStackTrace();
                flag = false;
            } catch (Exception e) {
                e.printStackTrace();
                flag = false;
            }
            mRecorder = null;
        }

        if (!flag) {
            Log.d(TAG, "stopRecord abnormal");
        }
        Log.d(TAG, "stopRecord end");
    }

    public void releaseRecord()
    {
        Log.d(TAG, "releaseRecord() start");
        if (mSurface != null) {
            mSurface.release();
        }
        Log.d(TAG, "releaseRecord end");
    }
}
