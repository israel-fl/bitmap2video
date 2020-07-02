package com.homesoft.bitmap2video

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.homesoft.bitmap2video.FileUtils.getVideoFile
import com.homesoft.bitmap2video.FileUtils.shareVideo
import com.homesoft.encoder.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/*
 * Copyright (C) 2020 Israel Flores
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

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = MainActivity::class.java.simpleName
        val imageArray: List<Int> = listOf(
                R.raw.im1,
                R.raw.im2,
                R.raw.im3,
                R.raw.im4
        )
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    private var videoFile: File? = null
    private var muxerConfig: MuxerConfig? = null
    private var mimeType = MediaFormat.MIMETYPE_VIDEO_AVC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        avc.isEnabled = isCodecSupported(mimeType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
        }

        setListeners()
    }

    private fun setListeners() {
        bt_make.setOnClickListener {
            bt_make.isEnabled = false

            basicVideoCreation()
        }

        avc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) setCodec(MediaFormat.MIMETYPE_VIDEO_AVC)
        }

        hevc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) setCodec(MediaFormat.MIMETYPE_VIDEO_HEVC)
        }

        bt_play.setOnClickListener {
            videoFile?.run {
                player.setVideoPath(this.absolutePath)
                player.start()
            }
        }

        bt_share.setOnClickListener {
            Log.i(TAG, "Sharing video...")
            muxerConfig?.run {
                shareVideo(this@MainActivity, file, mimeType)
            }
        }
    }

    private fun setCodec(codec: String) {
        if (isCodecSupported(codec)) {
            mimeType = codec
            muxerConfig?.mimeType = mimeType
        } else {
            Toast.makeText(this@MainActivity, "AVC Codec not supported", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    // Basic implementation
    private fun basicVideoCreation() {
        videoFile = getVideoFile(this@MainActivity, "test.mp4")
        videoFile?.run {
            muxerConfig = MuxerConfig(this, 600, 600, mimeType, 3, 1F, 1500000)
            val muxer = Muxer(this@MainActivity, muxerConfig!!)

            createVideo(muxer) // using callbacks
            // or
            createVideoAsync(muxer) // using co-routines
        }
    }

    // Callback-style approach
    private fun createVideo(muxer: Muxer) {
        muxer.setOnMuxingCompletedListener(object : MuxingCompletionListener {
            override fun onVideoSuccessful(file: File) {
                Log.d(TAG, "Video muxed - file path: ${file.absolutePath}")
                onMuxerCompleted()
            }

            override fun onVideoError(error: Throwable) {
                Log.e(TAG, "There was an error muxing the video")
                onMuxerCompleted()
            }
        })

        // Needs to happen on a background thread (long-running process)
        Thread(Runnable {
            muxer.mux(imageArray, R.raw.bensound_happyrock)
        }).start()
    }

    // Coroutine approach
    private fun createVideoAsync(muxer: Muxer) {
        scope.launch {
            when (val result = muxer.muxAsync(imageArray, R.raw.bensound_happyrock)) {
                is MuxingSuccess -> {
                    Log.i(TAG, "Video muxed - file path: ${result.file.absolutePath}")
                    onMuxerCompleted()
                }
                is MuxingError -> {
                    Log.e(TAG, "There was an error muxing the video")
                    bt_make.isEnabled = true
                }
            }
        }
    }

    private fun onMuxerCompleted() {
        runOnUiThread {
            bt_make.isEnabled = true
            bt_play.isEnabled = true
            bt_share.isEnabled = true
        }
    }
}
