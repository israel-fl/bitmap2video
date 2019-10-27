package com.homesoft.encoder;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import java.io.IOException;

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

public abstract class EncoderConfig {

    private final String mPath;
    private final int mWidth;
    private final int mHeight;
    private final float mFramesPerSecond;
    private final int mBitRate;

    abstract FrameMuxer getFrameMuxer() throws IOException;
    abstract MediaFormat getVideoMediaFormat();

    public static boolean isSupported(final String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public EncoderConfig(final String path, final int width, final int height, final float framesPerSecond, final int bitRate) {
        mPath = path;
        mWidth = width;
        mHeight = height;
        mFramesPerSecond = framesPerSecond;
        mBitRate = bitRate;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public float getFramePerSecond() {
        return mFramesPerSecond;
    }

    public String getPath() {
        return mPath;
    }

}
