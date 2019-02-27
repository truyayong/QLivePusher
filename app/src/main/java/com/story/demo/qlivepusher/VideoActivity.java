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
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnCompleteListener;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";

    private QCameraView mQCameraView;
    private Button mRecord;

    private MediaEncoder mMediaEncoder;

    private WlMusic mWlMusic;

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
        mWlMusic = WlMusic.getInstance();
        mWlMusic.setCallBackPcmData(true);
        mWlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                mWlMusic.playCutAudio(39, 60);
            }
        });

        mWlMusic.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                if (mMediaEncoder != null) {
                    mMediaEncoder.stopRecord();
                    mMediaEncoder = null;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecord.setText("开始录制");
                        }
                    });
                }
            }
        });

        mWlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
            @Override
            public void onPcmInfo(int samplerate, int bit, int channels) {
                Log.e(TAG, "onPcmInfo textureid : " + mQCameraView.getTextureId());
                mMediaEncoder = new MediaEncoder(VideoActivity.this, mQCameraView.getTextureId());
                mMediaEncoder.initEncodec(mQCameraView.getEglContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_live_pusher.mp4"
                        , 720, 1280, samplerate, channels);
                mMediaEncoder.setOnMediaInfoListener(new BaseMediaEncoder.OnMediaInfoListener() {
                    @Override
                    public void onMediaTime(int times) {
                        Log.e(TAG, "time is " + times);
                    }
                });
                mMediaEncoder.startRecord();
            }

            @Override
            public void onPcmData(byte[] pcmdata, int size, long clock) {
                if (mMediaEncoder != null) {
                    mMediaEncoder.putPCMData(pcmdata, size);
                }
            }
        });

    }

    private void record() {
        Log.e(TAG, "trecord ");
        if (mMediaEncoder == null) {
            mWlMusic.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/不仅仅是喜欢.ogg");
            mWlMusic.prePared();
            mRecord.setText("正在录制");
        } else {
            Log.e(TAG, "record stop ");
            mMediaEncoder.stopRecord();
            mRecord.setText("开始录制");
            mMediaEncoder = null;
            mWlMusic.stop();
        }
    }
}
