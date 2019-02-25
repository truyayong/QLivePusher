package com.story.demo.qlivepusher;

import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.story.demo.qlivepusher.camera.QCameraView;
import com.story.demo.qlivepusher.encodec.BaseMediaEncoder;
import com.story.demo.qlivepusher.encodec.MediaEncoder;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";

    private QCameraView mQCameraView;
    private Button mRecord;

    private MediaEncoder mMediaEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_video);
        mQCameraView = findViewById(R.id.camera);
        mRecord = findViewById(R.id.btn_record);
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record();
            }
        });
    }

    private void record() {
        Log.e(TAG, "trecord ");
        if (mMediaEncoder == null) {
            Log.e(TAG, "textureId is : " + mQCameraView.getTextureId());
            mMediaEncoder = new MediaEncoder(this, mQCameraView.getTextureId());
            mMediaEncoder.initEncodec(mQCameraView.getEglContext()
                    , Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_live_pusher.mp4"
                    , MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280);
            mMediaEncoder.setOnMediaInfoListener(new BaseMediaEncoder.OnMediaInfoListener() {
                @Override
                public void onMediaTime(int times) {
                    Log.e(TAG, "time is " + times);
                }
            });
            mMediaEncoder.startRecord();
            mRecord.setText("正在录制");
        } else {
            Log.e(TAG, "record stop ");
            mMediaEncoder.stopRecord();
            mRecord.setText("开始录制");
            mMediaEncoder = null;
        }
    }
}
