package com.story.demo.qlivepusher.imgvideo;

import android.content.Context;
import android.util.AttributeSet;

import com.story.demo.qlivepusher.egl.EGLSurfaceView;

/**
 * Created by qiuyayong on 2019/2/27.
 * Email:qiuyayong@bigo.sg
 */

public class ImgVideoView extends EGLSurfaceView {

    private ImgVideoRender mImgVideoRender;
    private int fbotextureid;
    public ImgVideoView(Context context) {
        this(context, null);
    }

    public ImgVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImgVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mImgVideoRender = new ImgVideoRender(context);
        setRender(mImgVideoRender);
        setRenderMode(EGLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mImgVideoRender.setOnRenderCreateListener(new ImgVideoRender.OnRenderCreateListener() {
            @Override
            public void onCreate(int textid) {
                fbotextureid = textid;
            }
        });
    }

    public void setCurrentImg(int imgSrc) {
        if (mImgVideoRender != null) {
            mImgVideoRender.setCurrentImgSrc(imgSrc);
            requestRender();
        }
    }

    public int getFbotextureid() {
        return fbotextureid;
    }
}
