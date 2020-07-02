package com.homesoft.encoder

import android.media.MediaFormat
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

data class MuxerConfig(
        var file: File,
        var videoWidth: Int = 320,
        var videoHeight: Int = 240,
        var mimeType: String = MediaFormat.MIMETYPE_VIDEO_AVC,
        var framesPerImage: Int = 1,
        var framesPerSecond: Float = 10F,
        var bitrate: Int = 1500000,
        var frameMuxer: FrameMuxer = Mp4FrameMuxer(file.absolutePath, framesPerSecond),
        var iFrameInterval: Int = 10
)

interface MuxingCompletionListener {
    fun onVideoSuccessful(file: File)
    fun onVideoError(error: Throwable)
}

interface MuxingResult

data class MuxingSuccess(
        val file: File
): MuxingResult

data class MuxingError(
        val message: String,
        val exception: Exception
): MuxingResult
