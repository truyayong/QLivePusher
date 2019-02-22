package com.story.demo.qlivepusher.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;

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
        mQCamera = new QCamera();
        setRender(mRender);
        mRender.setOnSurfaceCreateListener(new QCameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture) {
                mQCamera.startPreview(surfaceTexture, mCameraId);
            }
        });
    }
}
