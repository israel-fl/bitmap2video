package com.homesoft.bitmap2video;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.homesoft.encoder.EncoderConfig;
import com.homesoft.encoder.FrameEncoder;

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

public class CreateRunnable implements Runnable {
    private static final int[] IMAGE_IDS = {R.raw.im1, R.raw.im2, R.raw.im3, R.raw.im4};
    private static final int FRAMES = 30;
    private static final String TAG = CreateRunnable.class.getSimpleName();
    private final MainActivity mMainActivity;
    private final EncoderConfig mEncoderConfig;
    private final Paint mPaint;

    private String mOutputPath;

    CreateRunnable(final MainActivity activity, final EncoderConfig encoderConfig, final boolean addText) {
        mMainActivity = activity;
        mEncoderConfig = encoderConfig;
        if (addText) {
            mPaint = new Paint();
            mPaint.setColor(Color.WHITE);
        } else {
            mPaint = null;
        }
    }

    @Override
    public void run() {
        final FrameEncoder frameEncoder = new FrameEncoder(mEncoderConfig);
        try {
            frameEncoder.start();
        } catch (IOException e) {
            Log.e(TAG, "Start Encoder Failed", e);
            return;
        }
        mPaint.setTextSize(mEncoderConfig.getHeight() / 2);
        final Resources resources = mMainActivity.getResources();
        for (int i=0;i<FRAMES;i++) {
            final Bitmap bitmap = BitmapFactory.decodeStream(resources.openRawResource(IMAGE_IDS[i&3]));
            if (mPaint == null) {
                frameEncoder.createFrame(bitmap);
            } else {
                final Canvas canvas = frameEncoder.getCanvas();
                canvas.drawBitmap(bitmap, 0f, 0f, null);
                final String text = Character.toString((char)('A' + i));
                canvas.drawText(text, 0, mEncoderConfig.getHeight(), mPaint);
                frameEncoder.createFrame(canvas);
            }
        }
        mOutputPath = mEncoderConfig.getPath();
        frameEncoder.release();
        mMainActivity.done();
    }

    String getOutputPath() {
        return mOutputPath;
    }
}
