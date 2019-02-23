package com.story.demo.qlivepusher.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.WindowManager;

import com.story.demo.qlivepusher.egl.EGLSurfaceView;

/**
 * Created by qiuyayong on 2019/2/22.
 * Email:qiuyayong@bigo.sg
 */

public class QCameraView extends EGLSurfaceView {
    private static final String TAG = "QCameraView";

    private QCameraRender mRender;
    private QCamera mQCamera;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    public QCameraView(Context context) {
        this(context, null);
    }

    public QCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRender = new QCameraRender(context);
        mQCamera = new QCamera(context);
        setRender(mRender);
        previewAngle(context);
        mRender.setOnSurfaceCreateListener(new QCameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture) {
                mQCamera.startPreview(surfaceTexture, mCameraId);
            }
        });
    }

    public void onDestory() {
        if (mQCamera != null) {
            mQCamera.stoppreview();
        }
    }

    public void previewAngle(Context context) {
        int angle = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        mRender.resetMatrix();
        switch (angle) {
            case Surface.ROTATION_0:
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRender.setAngle(90, 0, 0, 1);
                    mRender.setAngle(180, 1, 0, 0);
                } else {
                    mRender.setAngle(90, 0, 0, 1);
                }
                break;
            case Surface.ROTATION_90:
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRender.setAngle(180, 0, 0, 1);
                    mRender.setAngle(180, 0, 1, 0);
                } else {
                    mRender.setAngle(90, 0, 1, 0);
                }
                break;
            case Surface.ROTATION_180:
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRender.setAngle(90, 0, 0, 1);
                    mRender.setAngle(180, 0, 1, 0);
                } else {
                    mRender.setAngle(-90,0,0,1);
                }
                break;
            case Surface.ROTATION_270:
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRender.setAngle(180, 0, 1, 0);
                } else {
                    mRender.setAngle(0, 0, 0, 1);
                }
                break;
            default:
                break;
        }
    }
}
