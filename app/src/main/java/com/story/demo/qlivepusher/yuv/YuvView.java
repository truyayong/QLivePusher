package com.story.demo.qlivepusher.yuv;

import android.content.Context;
import android.util.AttributeSet;

import com.story.demo.qlivepusher.egl.EGLSurfaceView;

/**
 * Created by qiuyayong on 2019/3/4.
 * Email:qiuyayong@bigo.sg
 */

public class YuvView extends EGLSurfaceView {
    private static final String TAG = "YuvView";

    private YuvRender mYuvRender;
    public YuvView(Context context) {
        this(context, null);
    }

    public YuvView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YuvView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mYuvRender = new YuvRender(context);
        setRender(mYuvRender);
        setRenderMode(EGLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setFrameData(int w, int h, byte[] by, byte[] bu, byte[] bv) {
        if (mYuvRender != null) {
            mYuvRender.setFrameData(w, h, by, bu, bv);
            requestRender();
        }
    }
}
