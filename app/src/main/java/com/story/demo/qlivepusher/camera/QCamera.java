package com.story.demo.qlivepusher.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.story.demo.qlivepusher.utils.DisplayUtil;

import java.io.IOException;
import java.util.List;

/**
 * Created by qiuyayong on 2019/2/22.
 * Email:qiuyayong@bigo.sg
 */

public class QCamera {
    private static final String TAG = "QCamera";

    private Camera mCamera;

    private SurfaceTexture mSurfaceTexture;
    private int mCameraId;

    private int width;
    private int height;

    public QCamera(Context context) {
        width = DisplayUtil.getScreenWidth(context);
        height = DisplayUtil.getScreenHeight(context);
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

            Camera.Size size = getFitSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(size.width, size.height);
            size = getFitSize(parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(size.width, size.height);

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

    private Camera.Size getFitSize(List<Camera.Size> sizes) {
        if (width < height) {
            int t = height;
            height = width;
            width = t;
        }

        for (Camera.Size size : sizes) {
            if(1.0f * size.width / size.height == 1.0f * width / height)
            {
                return size;
            }
        }
        return sizes.get(0);
    }
}
