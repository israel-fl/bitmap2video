package com.homesoft.bitmap2video;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.VideoView;

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

public class MainActivity extends Activity {
    private VideoView mVideoPlayer;
    private CreateRunnable mCreateRunnable;
    private Button mPlay;
    private RadioGroup mCodec;
    private RadioButton mAvc, mHevc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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


    void done() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlay.setEnabled(true);
            }
        });
    }
}
