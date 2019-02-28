package com.story.demo.qlivepusher;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.story.demo.qlivepusher.encodec.MediaEncoder;
import com.story.demo.qlivepusher.imgvideo.ImgVideoView;
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;

public class ImageVideoActivity extends AppCompatActivity {

    private ImgVideoView mImgVideoView;
    private MediaEncoder mMediaEncoder;
    private WlMusic mWlMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_video);
        mImgVideoView = findViewById(R.id.imgvideoview);
        mImgVideoView.setCurrentImg(R.drawable.img_1);

        mWlMusic = WlMusic.getInstance();
        mWlMusic.setCallBackPcmData(true);

        mWlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                mWlMusic.playCutAudio(0, 60);
            }
        });

        mWlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
            @Override
            public void onPcmInfo(int samplerate, int bit, int channels) {
                mMediaEncoder = new MediaEncoder(ImageVideoActivity.this, mImgVideoView.getFbotextureid());
                mMediaEncoder.initEncodec(mImgVideoView.getEglContext(),
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_image_video.mp4",
                        720, 500, samplerate, channels);
                mMediaEncoder.startRecord();
                startImgs();
            }

            @Override
            public void onPcmData(byte[] pcmdata, int size, long clock) {
                mMediaEncoder.putPCMData(pcmdata, size);
            }
        });
    }

    public void start(View view) {
        mWlMusic.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/the girl.m4a");
        mWlMusic.prePared();
    }

    private void startImgs() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 257; i++) {
                    int imgsrc = getResources().getIdentifier("img_" + i, "drawable", "com.story.demo.qlivepusher");
                    mImgVideoView.setCurrentImg(imgsrc);
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (mMediaEncoder != null) {
                    mWlMusic.stop();
                    mMediaEncoder.stopRecord();
                    mMediaEncoder = null;
                }
            }
        }).start();
    }
}
