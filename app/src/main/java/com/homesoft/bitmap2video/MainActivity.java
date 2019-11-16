package com.homesoft.bitmap2video;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.homesoft.drawable.PathRoundedRectShape;
import com.homesoft.encoder.AvcEncoderConfig;
import com.homesoft.encoder.EncoderConfig;
import com.homesoft.encoder.HevcEncoderConfig;

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

public class MainActivity extends AppCompatActivity {
    private VideoView mVideoPlayer;
    private CreateRunnable mCreateRunnable;
    private Button mPlay;
    private RadioGroup mCodec;
    private RadioButton mAvc, mHevc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createActionBarBorder(toolbar);
        findViewById(R.id.make).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EncoderConfig encoderConfig;
                final int radioId = mCodec.getCheckedRadioButtonId();
                if (radioId == mAvc.getId()) {
                    encoderConfig = new AvcEncoderConfig();
                } else if (radioId == mHevc.getId()) {
                    encoderConfig = new HevcEncoderConfig();
                } else {
                    return;
                }
                AsyncTask.THREAD_POOL_EXECUTOR.execute(mCreateRunnable = new CreateRunnable(MainActivity.this, encoderConfig, true));
            }
        });
        mVideoPlayer = findViewById(R.id.player);
        mPlay = findViewById(R.id.play);
        mPlay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mVideoPlayer.setVideoPath(mCreateRunnable.getOutputPath());
                mVideoPlayer.start();
            }
        });
        mCodec = findViewById(R.id.codec);
        mAvc = findViewById(R.id.avc);
        mHevc = findViewById(R.id.hevc);
        mAvc.setEnabled(EncoderConfig.isSupported(AvcEncoderConfig.MIME_TYPE));
        mHevc.setEnabled(EncoderConfig.isSupported(HevcEncoderConfig.MIME_TYPE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
        }
    }

    private static float getFloat(final Resources res, int id) {
        TypedValue typedValue = new TypedValue();
        res.getValue(id, typedValue, true);
        return typedValue.getFloat();
    }


    private void createActionBarBorder(final Toolbar toolbar) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            //This is not awesome, but shadowLayer is not supported by hardware acceleration before Pie
            toolbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        final Resources res = getResources();
        final PathRoundedRectShape shape = new PathRoundedRectShape();
        shape.setCornerRadius(res.getDimension(R.dimen.actionBarCornerRadius));
        final ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
        final int insets = res.getDimensionPixelSize(R.dimen.actionBarInsets);
        final InsetDrawable insetDrawable = new InsetDrawable(shapeDrawable, insets);
        final Paint paint = shapeDrawable.getPaint();
        paint.setStyle(Paint.Style.FILL);
        shape.setClip(true);
        paint.setShadowLayer(getFloat(res, R.dimen.actionBarShadowRadius),
                getFloat(res, R.dimen.actionBarShadowDx),
                getFloat(res, R.dimen.actionBarShadowDy),
                res.getColor(R.color.actionBarShadowColor));
        toolbar.setBackground(insetDrawable);
    }

    void done() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlay.setEnabled(true);
            }
        });
    }
}
