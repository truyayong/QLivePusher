package com.story.demo.qlivepusher.camera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;

/**
 * Created by qiuyayong on 2019/2/22.
 * Email:qiuyayong@bigo.sg
 */

public class QCamera {
    private static final String TAG = "QCamera";

    private Camera mCamera;

    private SurfaceTexture mSurfaceTexture;
    private int mCameraId;

    public QCamera() {
    }

    public void startPreview(SurfaceTexture surfaceTexture, int cameraId) {
        Log.i(TAG, " surfaceTexture : " + surfaceTexture + " cameraId : " + cameraId);
        this.mSurfaceTexture = surfaceTexture;
        this.mCameraId = cameraId;
        if (mSurfaceTexture == null) {
            return;
        }

        try {
            mCamera = Camera.open(mCameraId);
            mCamera.setPreviewTexture(mSurfaceTexture);
            Camera.Parameters parameters = mCamera.getParameters();

            parameters.setFlashMode("off");
            parameters.setPreviewFormat(ImageFormat.NV21);

            parameters.setPictureSize(parameters.getSupportedPictureSizes().get(0).width
                    , parameters.getSupportedPictureSizes().get(0).height);
            parameters.setPreviewSize(parameters.getSupportedPreviewSizes().get(0).width
                    , parameters.getSupportedPreviewSizes().get(0).height);

            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "startPreview Exception : ", e);
        }
    }

    public void stoppreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void changeCamera(int cameraId) {
        if (mCamera != null) {
            stoppreview();
        }
        this.mCameraId = cameraId;
        startPreview(mSurfaceTexture, mCameraId);
    }
}
