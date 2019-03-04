package com.story.demo.qlivepusher;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.story.demo.qlivepusher.yuv.YuvView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class YuvActivity extends AppCompatActivity {
    private static final String TAG = "YuvActivity";

    private YuvView mYuvView;

    private FileInputStream fis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yuv);
        mYuvView = findViewById(R.id.yuvview);
    }

    public void start(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int w = 640;
                    int h = 360;
                    fis = new FileInputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sintel_640_360.yuv"));
                    byte []y = new byte[w * h];
                    byte []u = new byte[w * h / 4];
                    byte []v = new byte[w * h / 4];

                    while (true)
                    {
                        int ry = fis.read(y);
                        int ru = fis.read(u);
                        int rv = fis.read(v);
                        if(ry > 0 && ru > 0 && rv > 0)
                        {
                            mYuvView.setFrameData(w, h, y, u, v);
                            Thread.sleep(40);
                        }
                        else
                        {
                            Log.d(TAG, "完成");
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
