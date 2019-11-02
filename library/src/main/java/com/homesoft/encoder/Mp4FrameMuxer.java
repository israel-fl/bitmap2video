package com.homesoft.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

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

public class Mp4FrameMuxer implements FrameMuxer {
    private final long mFrameUsec;
    private final MediaMuxer mMuxer;

    private boolean mStarted;
    private int mVideoTrackIndex;
    private int mFrame;

    public Mp4FrameMuxer(final String path, final float fps) throws IOException {
        mFrameUsec = FrameEncoder.getFrameTime(fps);
        mMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    @Override
    public boolean isStarted() {
        return mStarted;
    }

    @Override
    public void start(FrameEncoder frameEncoder) {
        MediaFormat newFormat = frameEncoder.getVideoMediaCodec().getOutputFormat();

        // now that we have the Magic Goodies, start the muxer
        mVideoTrackIndex = mMuxer.addTrack(newFormat);
        mMuxer.start();
        mStarted = true;
    }

    @Override
    public void muxVideoFrame(ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        // adjust the ByteBuffer values to match BufferInfo (not needed?)
        encodedData.position(bufferInfo.offset);
        encodedData.limit(bufferInfo.offset + bufferInfo.size);

        //This code will break if the encoder supports B frames.
        //Ideally we would use set the value in the encoder,
        //don't know how to do that without using OpenGL
        bufferInfo.presentationTimeUs = mFrameUsec * mFrame++;
        mMuxer.writeSampleData(mVideoTrackIndex, encodedData, bufferInfo);
    }

    @Override
    public void release() {
        mMuxer.stop();
        mMuxer.release();
    }
}
