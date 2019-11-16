# bitmap2video
Generate video from Bitmaps or a Canvas in Android.

Create mp4 video from Bitmaps or anything you can draw to a hardware accelerated Canvas.  Pure, simple Android MediaCodec implementation.  Requires no third party libs or NDK.

Currently supports the MP4 container and both AVC/H264 and HEVC/H265.  Easily extensable to other supported formats.  

Run the sample app or check out [CreateRunnable](app/src/main/java/com/homesoft/bitmap2video/CreateRunnable.java) for an example.

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
