package com.story.demo.qlivepusher.encodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import com.story.demo.qlivepusher.egl.EGLSurfaceView;
import com.story.demo.qlivepusher.egl.EglHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by qiuyayong on 2019/2/25.
 * Email:qiuyayong@bigo.sg
 */

public abstract class BaseMediaEncoder {
    private static final String TAG = "BaseMediaEncoder";

    private Surface mSurface;
    private EGLContext mEglContext;

    private int width;
    private int height;

    private MediaCodec mVideoMediaCodec;
    private MediaFormat mVideoMediaFormat;
    private MediaCodec.BufferInfo mVideoBuffer;

    private MediaCodec mAudioMediaCodec;
    private MediaFormat mAudioMediaFormat;
    private MediaCodec.BufferInfo mAudioBuffer;
    private long mAudioPts = 0;
    private int mSampleRate;

    private MediaMuxer mMediaMuxer;
    private boolean mEncodecStart;
    private boolean mAudioExit;
    private boolean mVideoExit;

    private EGLSurfaceView.QGLRender mRender;

    private EGLVideoThread mEglVideoThread;
    private VideoMediaThread mVideoMediaThread;
    private AudioMediaThread mAudioMediaThread;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    private OnMediaInfoListener mOnMediaInfoListener;

    public void setOnMediaInfoListener(OnMediaInfoListener listener) {
        this.mOnMediaInfoListener = listener;
    }

    public void setRender(EGLSurfaceView.QGLRender render) {
        this.mRender = render;
    }

    public void setRenderMode(int renderMode) {
        if (mRender == null) {
            throw  new RuntimeException("BaseMediaEncoder must set render before");
        }
        this.mRenderMode = renderMode;
    }

    public void initEncodec(EGLContext eglContext, String savePath, int width, int height, int samplerate, int channelCount) {
        this.width = width;
        this.height = height;
        this.mEglContext = eglContext;

        try {
            mMediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            initVideoEncodec(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            initAudioEncodec(MediaFormat.MIMETYPE_AUDIO_AAC, samplerate, channelCount);

        } catch (IOException e) {
            mMediaMuxer = null;
            mVideoMediaCodec = null;
            mVideoMediaFormat = null;
            mVideoBuffer = null;
            mSurface = null;
            Log.e(TAG, "initEncodec Exception : ", e);
        }
    }

    private void initVideoEncodec(String mimeType, int width, int height) throws IOException {
        mVideoBuffer = new MediaCodec.BufferInfo();
        mVideoMediaFormat = MediaFormat.createVideoFormat(mimeType, width, height);
        mVideoMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mVideoMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4);
        mVideoMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mVideoMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        mVideoMediaCodec = MediaCodec.createEncoderByType(mimeType);
        mVideoMediaCodec.configure(mVideoMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        mSurface = mVideoMediaCodec.createInputSurface();
    }

    private void initAudioEncodec(String mimeType, int samplerate, int channelCount) throws IOException {
        this.mSampleRate = samplerate;
        mAudioBuffer = new MediaCodec.BufferInfo();
        mAudioMediaFormat = MediaFormat.createAudioFormat(mimeType, samplerate, channelCount);
        mAudioMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
        mAudioMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mAudioMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096);

        mAudioMediaCodec = MediaCodec.createEncoderByType(mimeType);
        mAudioMediaCodec.configure(mAudioMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public void startRecord() {
        Log.e(TAG, "startRecord mSurface : " + mSurface + " mEglContext : " + mEglContext);
        if (mSurface != null && mEglContext != null) {
            mAudioPts = 0;
            mAudioExit = false;
            mVideoExit = false;
            mEncodecStart = false;

            mEglVideoThread = new EGLVideoThread(new WeakReference<BaseMediaEncoder>(this));
            mVideoMediaThread = new VideoMediaThread(new WeakReference<BaseMediaEncoder>(this));
            mAudioMediaThread = new AudioMediaThread(new WeakReference<BaseMediaEncoder>(this));
            mEglVideoThread.isCreate = true;
            mEglVideoThread.isChange = true;
            mEglVideoThread.start();
            mVideoMediaThread.start();
            mAudioMediaThread.start();
        }
    }

    public void stopRecord() {
        if (mAudioMediaThread != null && mVideoMediaThread != null && mEglVideoThread != null) {
            mVideoMediaThread.exit();
            mAudioMediaThread.exit();
            mEglVideoThread.onDestory();
            mVideoMediaThread = null;
            mAudioMediaThread = null;
            mEglVideoThread = null;
        }
    }

    static class VideoMediaThread extends Thread {

        private WeakReference<BaseMediaEncoder> mReference;

        private boolean isExist;

        private MediaCodec videoMediaCodec;
        private MediaFormat videoMediaFormat;
        private MediaCodec.BufferInfo videoBufferInfo;
        private MediaMuxer mediaMuxer;

        private int videoTrackIndex;
        private long pts;

        public VideoMediaThread(WeakReference<BaseMediaEncoder> reference) {
            this.mReference = reference;
            videoMediaCodec = reference.get().mVideoMediaCodec;
            videoBufferInfo = reference.get().mVideoBuffer;
            videoMediaFormat = reference.get().mVideoMediaFormat;
            mediaMuxer = reference.get().mMediaMuxer;
        }

        @Override
        public void run() {
            super.run();
            Log.e(TAG, "VideoMediaThread run ");
            pts = 0;
            videoTrackIndex = -1;
            isExist = false;
            videoMediaCodec.start();
            while (true) {
                if (isExist) {
                    videoMediaCodec.stop();
                    videoMediaCodec.release();
                    videoMediaCodec = null;
                    mReference.get().mVideoExit = true;

                    if (mReference.get().mAudioExit) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;
                    }
                    Log.e(TAG, "录制完成");
                    break;
                }

                int outputBufferIndex = videoMediaCodec.dequeueOutputBuffer(videoBufferInfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    videoTrackIndex = mediaMuxer.addTrack(videoMediaCodec.getOutputFormat());
                    if (mReference.get().mAudioMediaThread.audioTrackIndex != -1) {
                        mediaMuxer.start();
                        mReference.get().mEncodecStart = true;
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        if (mReference.get().mEncodecStart) {
                            ByteBuffer outputBuffer = videoMediaCodec.getOutputBuffers()[outputBufferIndex];
                            outputBuffer.position(videoBufferInfo.offset);
                            outputBuffer.limit(videoBufferInfo.offset + videoBufferInfo.size);

                            if (pts == 0) {
                                pts = videoBufferInfo.presentationTimeUs;
                            }
                            videoBufferInfo.presentationTimeUs = videoBufferInfo.presentationTimeUs - pts;

                            mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer, videoBufferInfo);

                            if (mReference.get().mOnMediaInfoListener != null) {
                                mReference.get().mOnMediaInfoListener.onMediaTime((int) (videoBufferInfo.presentationTimeUs / 1000000));
                            }
                        }
                        videoMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = videoMediaCodec.dequeueOutputBuffer(videoBufferInfo, 0);
                    }
                }
            }
        }

        public void exit() {
            isExist = true;
        }
    }

    static class AudioMediaThread extends Thread {
        private WeakReference<BaseMediaEncoder> mReference;

        private boolean isExist;

        private MediaCodec audioMediaCodec;
        private MediaFormat audioMediaFormat;
        private MediaCodec.BufferInfo audioBufferInfo;
        private MediaMuxer mediaMuxer;

        private int audioTrackIndex;
        private long pts;

        public AudioMediaThread(WeakReference<BaseMediaEncoder> reference) {
            this.mReference = reference;
            audioMediaCodec = mReference.get().mAudioMediaCodec;
            audioMediaFormat = mReference.get().mAudioMediaFormat;
            audioBufferInfo = mReference.get().mAudioBuffer;
            mediaMuxer = mReference.get().mMediaMuxer;
            audioTrackIndex = -1;
        }

        @Override
        public void run() {
            super.run();
            pts = 0;
            isExist = false;
            audioMediaCodec.start();
            while (true) {
                if (isExist) {
                    audioMediaCodec.stop();
                    audioMediaCodec.release();
                    audioMediaCodec = null;
                    mReference.get().mAudioExit = true;

                    if (mReference.get().mVideoExit) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;
                    }
                    break;
                }

                int outputBufferIndex = audioMediaCodec.dequeueOutputBuffer(audioBufferInfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    audioTrackIndex = mediaMuxer.addTrack(audioMediaCodec.getOutputFormat());
                    if (mReference.get().mVideoMediaThread.videoTrackIndex != -1) {
                        mediaMuxer.start();
                        mReference.get().mEncodecStart = true;
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        if (mReference.get().mEncodecStart) {
                            ByteBuffer outputBuffer = audioMediaCodec.getOutputBuffers()[outputBufferIndex];
                            outputBuffer.position(audioBufferInfo.offset);
                            outputBuffer.limit(audioBufferInfo.offset + audioBufferInfo.size);
                            if (pts == 0) {
                                pts = audioBufferInfo.presentationTimeUs;
                            }
                            audioBufferInfo.presentationTimeUs = audioBufferInfo.presentationTimeUs - pts;
                            mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer, audioBufferInfo);
                        }
                        audioMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = audioMediaCodec.dequeueOutputBuffer(audioBufferInfo, 0);
                    }
                }
            }
        }

        public void exit() {
            isExist = true;
        }
    }

    public void putPCMData(byte[] buffer, int size) {
        if (mAudioMediaThread != null && !mAudioMediaThread.isExist && buffer != null && size > 0) {
            int inputBufferIndex = mAudioMediaCodec.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer byteBuffer = mAudioMediaCodec.getInputBuffers()[inputBufferIndex];
                byteBuffer.clear();
                byteBuffer.put(buffer);
                long pts = getAudioPts(size, mSampleRate);
                mAudioMediaCodec.queueInputBuffer(inputBufferIndex, 0, size, pts, 0);
            }
        }
    }

    private long getAudioPts(int size, int sampleRate) {
        mAudioPts += (long) (1.0 * size / (sampleRate * 2 * 2) * 1000000.0);
        return mAudioPts;
    }

    static class EGLVideoThread extends Thread {
        private WeakReference<BaseMediaEncoder> reference;
        private EglHelper mEglHelper;
        private Object object;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        public EGLVideoThread(WeakReference<BaseMediaEncoder> reference) {
            this.reference = reference;
        }

        @Override
        public void run() {
            super.run();
            Log.e(TAG, "EGLVideoThread run ");
            isExit = false;
            isStart = false;
            object = new Object();
            mEglHelper = new EglHelper();
            mEglHelper.initEgl(reference.get().mSurface, reference.get().mEglContext);

            while (true) {
                if (isExit) {
                    release();
                    break;
                }

                if (isStart) {
                    if(reference.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (object)
                        {
                            try {
                                object.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if(reference.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw  new RuntimeException("BaseMediaEncoder mRenderMode is wrong value");
                    }
                }
                onCreate();
                onChange(reference.get().width, reference.get().height);
                onDraw();
                isStart = true;
            }
        }

        private void onCreate() {
            if(isCreate && reference.get().mRender != null)
            {
                isCreate = false;
                reference.get().mRender.onSurfaceCreate();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && reference.get().mRender != null) {
                isChange = false;
                reference.get().mRender.onSurfaceChanged(width, height);
            }
        }

        private void onDraw() {
            if(reference.get().mRender != null && mEglHelper != null)
            {
                reference.get().mRender.onDrawFrame();
                if(!isStart)
                {
                    reference.get().mRender.onDrawFrame();
                }
                mEglHelper.swapBuffers();

            }
        }

        private void requestRender() {
            if (object != null) {
                synchronized (object) {
                    object.notifyAll();
                }
            }
        }

        public void onDestory() {
            isExit = true;
            requestRender();
        }

        private void release() {
            mEglHelper.destoryEgl();
            mEglHelper = null;
            object = null;
            reference = null;
        }
    }

    public interface OnMediaInfoListener {
        void onMediaTime(int times);
    }
}
