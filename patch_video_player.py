import re

with open("app/src/main/java/com/example/feature/course/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

old_exoplayer_creation = """    // Media3 ExoPlayer Instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).setMediaSourceFactory(DefaultMediaSourceFactory(DownloadUtil.getCacheDataSourceFactory(context))).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            
            val prefs = context.getSharedPreferences("video_prefs", Context.MODE_PRIVATE)
            val savedPosition = prefs.getLong(videoUrl, 0L)
            if (savedPosition > 0) {
                seekTo(savedPosition)
            }
            
            prepare()
            playWhenReady = true
        }
    }"""

new_exoplayer_creation = """    // Media3 ExoPlayer Instance
    val exoPlayer = remember {
        val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                32000, 
                64000, 
                2000, 
                2000
            ).build()
            
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(androidx.media3.exoplayer.source.DefaultMediaSourceFactory(DownloadUtil.getCacheDataSourceFactory(context)))
            .setLoadControl(loadControl)
            .build().apply {
                
            var mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            try {
                val downloadManager = DownloadUtil.getDownloadManager(context)
                val cursor = downloadManager.downloadIndex.getDownloads(androidx.media3.exoplayer.offline.Download.STATE_COMPLETED)
                while (cursor.moveToNext()) {
                    val download = cursor.download
                    if (download.request.uri.toString() == videoUrl || download.request.id.startsWith(lectureId)) {
                        mediaItem = download.request.toMediaItem()
                        break
                    }
                }
                cursor.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            setMediaItem(mediaItem)
            
            val prefs = context.getSharedPreferences("video_prefs", Context.MODE_PRIVATE)
            val savedPosition = prefs.getLong(videoUrl, 0L)
            if (savedPosition > 0) {
                seekTo(savedPosition)
            }
            
            prepare()
            playWhenReady = true
        }
    }"""

if old_exoplayer_creation in content:
    content = content.replace(old_exoplayer_creation, new_exoplayer_creation)
    with open("app/src/main/java/com/example/feature/course/VideoPlayerScreen.kt", "w") as f:
        f.write(content)
    print("Patched ExoPlayer creation successfully")
else:
    print("Could not find old ExoPlayer creation")
