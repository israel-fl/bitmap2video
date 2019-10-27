package com.homesoft.encoder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/*
 * Copyright (C) 2019 Homesoft, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class FrameEncoder {
    private static final String TAG = FrameEncoder.class.getSimpleName();
    private static final boolean VERBOSE = false;

    private final static long SECOND_IN_USEC = 1000000;

    private static final int TIMEOUT_USEC = 10000;

    private final EncoderConfig mEncoderConfing;

    private MediaCodec.BufferInfo mBufferInfo;
    private MediaCodec mEncoder;
    private Surface mSurface;
    private Rect mCanvasRect;

    private FrameMuxer mFrameMuxer;

    public static long getFrameTime(final float framesPerSecond) {
        return (long)(SECOND_IN_USEC / framesPerSecond);
    }

    public FrameEncoder(final EncoderConfig encoderConfig) {
        mEncoderConfing = encoderConfig;
    }

    public void start() throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();
        final MediaFormat mediaFormat = mEncoderConfing.getVideoMediaFormat();
        mEncoder = MediaCodec.createEncoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
        mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mCanvasRect = new Rect(0,0,mEncoderConfing.getWidth(), mEncoderConfing.getHeight());
        }
        mSurface = mEncoder.createInputSurface();
        mFrameMuxer = mEncoderConfing.getFrameMuxer();
        mEncoder.start();
        drainEncoder(false);
    }

    MediaCodec getVideoMediaCodec() {
        return mEncoder;
    }

    public Canvas getCanvas() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mSurface.lockHardwareCanvas();
        } else {
            return mSurface.lockCanvas(mCanvasRect);
        }
    }

    public void createFrame(final Bitmap bitmap) {
        final Canvas canvas = getCanvas();
        canvas.drawBitmap(bitmap, 0f, 0f, null);
        createFrame(canvas);
    }

    /**
     *
     * @param canvas aquired from getCanvas()
     */
    public void createFrame(final Canvas canvas) {
        mSurface.unlockCanvasAndPost(canvas);
        drainEncoder(false);
    }

    /**
     * Extracts all pending data from the encoder.
     * <p>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     */
    private void drainEncoder(boolean endOfStream) {
        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mFrameMuxer.isStarted()) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mFrameMuxer.start(this);
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mFrameMuxer.isStarted()) {
                        throw new RuntimeException("muxer hasn't started");
                    }
                    mFrameMuxer.muxVideoFrame(encodedData, mBufferInfo);
                    if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     */
    public void release() {
        if (VERBOSE) Log.d(TAG, "releasing encoder objects");
        if (mEncoder != null) {
            //This line could be isn't only call, like flush() or something
            drainEncoder(true);
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mFrameMuxer != null) {
            mFrameMuxer.release();
            mFrameMuxer = null;
        }
    }

}
