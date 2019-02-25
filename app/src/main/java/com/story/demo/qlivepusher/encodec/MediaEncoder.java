package com.story.demo.qlivepusher.encodec;

import android.content.Context;

/**
 * Created by qiuyayong on 2019/2/25.
 * Email:qiuyayong@bigo.sg
 */

public class MediaEncoder extends BaseMediaEncoder {

    private EncoderRender mRender;

    public MediaEncoder(Context context, int textureId) {
        mRender = new EncoderRender(context, textureId);
        setRender(mRender);
        setRenderMode(BaseMediaEncoder.RENDERMODE_CONTINUOUSLY);
    }
}
