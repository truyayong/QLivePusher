package com.story.demo.qlivepusher;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.story.demo.qlivepusher.camera.QCameraView;

public class CameraActivity extends AppCompatActivity {

    private QCameraView mQCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mQCameraView = findViewById(R.id.qcamera);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQCameraView.onDestory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mQCameraView.previewAngle(this);
    }
}
