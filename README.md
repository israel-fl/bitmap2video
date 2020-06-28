# bitmap2video
![](bitmap2video.gif)

Generate video from Bitmaps or a Canvas in Android.

Create mp4 video from Bitmaps or anything you can draw to a hardware accelerated Canvas.  Pure, simple Android MediaCodec implementation.  Requires no third party libs or NDK.

Currently supports the MP4 container and both AVC/H264 and HEVC/H265.  Easily extensable to other supported formats.  

Run the sample app or check out
[CreateRunnable](app/src/main/java/com/homesoft/bitmap2video/CreateRunnable.java)
and [MainActivity](app/src/main/java/com/homesoft/bitmap2video/MainActivity.java)
for an example.

# Dependencies
Add it in your root build.gradle at the end of repositories:

    allprojects {
      repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
  	}
  
  Add to your app dependancies:

    dependencies {
      implementation 'com.github.dburckh:bitmap2video:1.0.0'
    }


# Initialize library
Create an `EncoderConfig` object with your specific requirements.

```java
final EncoderConfig encoderConfig;
encoderConfig = new AvcEncoderConfig(FPS, BITRATE_DEFAULT);
// or
encoderConfig = new HevcEncoderConfig(FPS, BITRATE_DEFAULT);

videoFile = getVideoFile(MainActivity.this, "test.mp4");
encoderConfig.setPath(videoFile.getAbsolutePath());
encoderConfig.setFramesPerImage(FRAMES_PER_IMAGE);
encoderConfig.setHeight(DEFAULT_HEIGHT);
encoderConfig.setWidth(DEFAULT_WIDTH);
```

Create an instance of the `FrameEncoder` and pass in your
`EncoderConfig`

```java
final FrameEncoder frameEncoder = new FrameEncoder(context, encoderConfig);
frameEncoder.start();
```

## Encode

```java
final Bitmap bitmap = BitmapFactory.decodeStream(resources.openRawResource(R.drawable.image1));
frameEncoder.createFrame(bitmap);

frameEncoder.releaseVideoEncoder();

frameEncoder.releaseMuxer();
```

## Add audio 
Set an `AssetFileDescriptor` for your audio track on the
`EncoderConfig`
```java
encoderConfig.setAudioTrackFileDescriptor(getFileDescriptor(context, R.raw.sound_file));
```

#### Mux in the audio frames
Muxing of the audio frames needs to happen after you release the
`MediaCodec`

```java
frameEncoder.releaseVideoEncoder();

// Mux in the audio after we release the video encoder
frameEncoder.muxAudioFrames();

frameEncoder.releaseMuxer();
```

### Convenience utility functions
We provide a few functions to simplify a couple of tasks. These can be
found as static methods under `FileUtils`

##### Get an `AssetFileDescriptor` from a raw resource
`getFileDescriptor(Context context, int R.raw.sound_file)`

##### Get a `File` object for your video
`getVideoFile(final Context context, final String fileName)`

##### Export the created file to other applications
`shareVideo(Context context, File file, String mimeType)`

##### Note
These utility functions use the library's `ContentProvider` with
declared authorities and specified paths.

#### Declare your own ContentProvider
You can change which provider is being used and specify your paths if
you register your own against the manifest and create your own
resources. You must then pass in the appropriate paths to the functions.
See the library's
[AndroidManifest](app/src/main/java/com/homesoft/bitmap2video/library/src/main/AndroidManifest.xml)
for an example.

```java
getVideoFile(Context context, String fileDir, String fileName)
```
and
```java
shareVideo(Context context, File file, String mimeType, String fileAuthority)
```
