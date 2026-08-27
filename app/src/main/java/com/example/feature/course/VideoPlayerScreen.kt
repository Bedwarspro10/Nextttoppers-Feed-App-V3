@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)
package com.example.feature.course


import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import kotlin.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.draw.scale
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.GlobalQuickMenu
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadEntity
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.core.download.DownloadUtil
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.core.designsystem.HyperOsMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    videoTitle: String,
    lectureId: String = "",
    courseId: String = "",
    downloadRepository: com.example.core.download.DownloadRepository? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as? Activity
    
    // Player State
    var isPlaying by remember { mutableStateOf(true) }
    var playbackState by remember { mutableStateOf(Player.STATE_IDLE) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var bufferedPosition by remember { mutableStateOf(0L) }
    var isVideoEnded by remember { mutableStateOf(false) }
    

    // Menus
    var showSpeedMenu by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableStateOf(1f) }
    var showQualityMenu by remember { mutableStateOf(false) }
    var videoQualities by remember { mutableStateOf(listOf<Format>()) }
    
    var showDownloadSheet by remember { mutableStateOf(false) }
    var availableDownloadQualities by remember { mutableStateOf(listOf<String>()) }
    var isCheckingQualities by remember { mutableStateOf(false) }
    
    
    val downloads by (downloadRepository?.getDownloadsForLecture(lectureId) ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .collectAsState(initial = emptyList())
        
    val isAlreadyDownloaded = downloads.any { it.status == androidx.media3.exoplayer.offline.Download.STATE_COMPLETED }


    // Media3 ExoPlayer Instance
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
    }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Lifecycle Management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Only resume if it wasn't paused manually
                    if (isPlaying) {
                        exoPlayer.play()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            val prefs = context.getSharedPreferences("video_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong(videoUrl, exoPlayer.currentPosition).apply()
            
            lifecycleOwner.lifecycle.removeObserver(observer)

            exoPlayer.release()
            
            // Restore window settings on exit
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.setDecorFitsSystemWindows(act.window, true)
                WindowInsetsControllerCompat(act.window, act.window.decorView).show(WindowInsetsCompat.Type.systemBars())
                act.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
    
    // Player Listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingChange: Boolean) {
                isPlaying = isPlayingChange
            }
            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
                isVideoEnded = state == Player.STATE_ENDED
                if (state == Player.STATE_READY) {
                    hasError = false
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                val formats = mutableListOf<Format>()
                for (group in tracks.groups) {
                    if (group.type == C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            if (format.height > 0) {
                                formats.add(format)
                            }
                        }
                    }
                }
                videoQualities = formats.distinctBy { it.height }.sortedByDescending { it.height }
            }
            override fun onPlayerError(error: PlaybackException) {
                hasError = true
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }
    
    // Progress Tracker
    LaunchedEffect(exoPlayer, playbackState, isPlaying) {
        while (true) {
            if (playbackState == Player.STATE_READY && isPlaying) {
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
                bufferedPosition = exoPlayer.bufferedPosition.coerceAtLeast(0L)
                duration = exoPlayer.duration.coerceAtLeast(0L)
            }
            delay(500)
        }
    }
    
    // Auto-hide controls
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    // Fullscreen back handler
    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
        activity?.let { act ->
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            WindowCompat.setDecorFitsSystemWindows(act.window, true)
            WindowInsetsControllerCompat(act.window, act.window.decorView).show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls }
                )
            }
    ) {
        // Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // We use our own UI
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Error Overlay
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Unable to play this lecture", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            hasError = false
                            exoPlayer.prepare()
                            exoPlayer.play()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
            return
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = showControls || !isPlaying || isVideoEnded,
            enter = fadeIn(HyperOsMotion.fastTween),
            exit = fadeOut(HyperOsMotion.fastTween),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                // Bottom gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isFullscreen) {
                            isFullscreen = false
                            activity?.let { act ->
                                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                WindowCompat.setDecorFitsSystemWindows(act.window, true)
                                WindowInsetsControllerCompat(act.window, act.window.decorView).show(WindowInsetsCompat.Type.systemBars())
                            }
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = videoTitle,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(onClick = { /* TODO: More Menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                }

                // Center Controls
                if (!isVideoEnded) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind 10s
                        var rewindScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // Custom ripple/scale
                                ) {
                                    val target = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                    exoPlayer.seekTo(target)
                                    currentPosition = target
                                    showControls = true
                                    coroutineScope.launch {
                                        rewindScale = 1.2f
                                        delay(150)
                                        rewindScale = 1f
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(rewindScale)) {
                                Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                        }
                        
                        // Play/Pause
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                                .clickable {
                                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                    showControls = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        // Forward 10s
                        var forwardScale by remember { mutableStateOf(1f) }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val target = (exoPlayer.currentPosition + 10000).coerceAtMost(duration)
                                    exoPlayer.seekTo(target)
                                    currentPosition = target
                                    showControls = true
                                    coroutineScope.launch {
                                        forwardScale = 1.2f
                                        delay(150)
                                        forwardScale = 1f
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(forwardScale)) {
                                Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                } else {
                    // Video Ended
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                exoPlayer.seekTo(0)
                                exoPlayer.play()
                                isVideoEnded = false
                            },
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "Replay", tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Lecture completed", color = Color.White, fontSize = 16.sp)
                    }
                }

                // Bottom Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isFullscreen) 32.dp else 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    // Seekbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(currentPosition) + " / " + formatDuration(duration),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Box {
                                TextButton(onClick = { if (videoQualities.isNotEmpty()) showQualityMenu = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quality", color = Color.White, fontSize = 12.sp)
                                }
                                DropdownMenu(
                                    expanded = showQualityMenu,
                                    onDismissRequest = { showQualityMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Auto") },
                                        onClick = {
                                            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                                .build()
                                            showQualityMenu = false
                                        }
                                    )
                                    videoQualities.forEach { format ->
                                        DropdownMenuItem(
                                            text = { Text("${format.height}p") },
                                            onClick = {
                                                val trackGroup = exoPlayer.currentTracks.groups.find { group ->
                                                    (0 until group.length).any { i -> group.getTrackFormat(i).height == format.height }
                                                }
                                                if (trackGroup != null) {
                                                    val override = TrackSelectionOverride(trackGroup.mediaTrackGroup, listOf(0))
                                                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters.buildUpon()
                                                        .setOverrideForType(override)
                                                        .build()
                                                }
                                                showQualityMenu = false
                                            }
                                        )
                                    }
                                }
                            }


                            if (lectureId.isNotEmpty() && downloadRepository != null) {
                                Box {
                                    TextButton(onClick = { 
                                        if (isAlreadyDownloaded) {
                                            android.widget.Toast.makeText(context, "Already downloaded", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            isCheckingQualities = true
                                            showDownloadSheet = true
                                            coroutineScope.launch {
                                                availableDownloadQualities = downloadRepository.getAvailableQualities(videoUrl)
                                                isCheckingQualities = false
                                            }
                                        }
                                    }) {
                                        Icon(if (isAlreadyDownloaded) Icons.Default.Check else Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isAlreadyDownloaded) "Downloaded" else "Download", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                                                        Box {
                                TextButton(onClick = { showSpeedMenu = true }) {
                                    Text("${currentSpeed}×", color = Color.White, fontSize = 12.sp)
                                }
                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false }
                                ) {
                                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { speed ->
                                        DropdownMenuItem(
                                            text = { Text("${speed}×") },
                                            onClick = {
                                                currentSpeed = speed
                                                exoPlayer.playbackParameters = PlaybackParameters(speed)
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = {
                                isFullscreen = !isFullscreen
                                activity?.let { act ->
                                    if (isFullscreen) {
                                        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                        WindowCompat.setDecorFitsSystemWindows(act.window, false)
                                        WindowInsetsControllerCompat(act.window, act.window.decorView).hide(WindowInsetsCompat.Type.systemBars())
                                        act.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                    } else {
                                        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                        WindowCompat.setDecorFitsSystemWindows(act.window, true)
                                        WindowInsetsControllerCompat(act.window, act.window.decorView).show(WindowInsetsCompat.Type.systemBars())
                                        act.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    
                    Slider(
                        value = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()) else 0f,
                        onValueChange = { value ->
                            val target = (value * duration).toLong()
                            currentPosition = target
                            showControls = true
                        },
                        onValueChangeFinished = {
                            exoPlayer.seekTo(currentPosition)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
        
        // Buffering Indicator
        if (playbackState == Player.STATE_BUFFERING) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

    if (showDownloadSheet) {
        ModalBottomSheet(onDismissRequest = { showDownloadSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Download Lecture",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = videoTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (isCheckingQualities) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (availableDownloadQualities.isEmpty()) {
                    Text("Quality selection is not available for this video.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                downloadRepository?.addDownload(
                                    lectureId = lectureId,
                                    title = videoTitle,
                                    subject = courseId, // Using courseId as a subject placeholder
                                    url = videoUrl,
                                    quality = "Auto"
                                )
                                showDownloadSheet = false
                                android.widget.Toast.makeText(context, "Download started", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Download Anyway")
                    }
                } else {
                    Text("Select Quality", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val allowedQualities = availableDownloadQualities.filter { it == "480p" || it == "720p" }
                    val qualitiesToShow = if (allowedQualities.isEmpty()) availableDownloadQualities else allowedQualities
                    
                    qualitiesToShow.forEach { quality ->
                        val existingDownload = downloads.find { it.selectedQuality == quality }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (existingDownload == null || existingDownload.status == androidx.media3.exoplayer.offline.Download.STATE_FAILED) {
                                        coroutineScope.launch {
                                            downloadRepository?.addDownload(
                                                lectureId = lectureId,
                                                title = videoTitle,
                                                subject = courseId, // using courseId for subject
                                                url = videoUrl,
                                                quality = quality
                                            )
                                            showDownloadSheet = false
                                            android.widget.Toast.makeText(context, "Download started", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(quality, style = MaterialTheme.typography.bodyLarge)
                            if (existingDownload != null && existingDownload.status != androidx.media3.exoplayer.offline.Download.STATE_FAILED) {
                                Icon(Icons.Default.Check, contentDescription = "Downloaded", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    }
}

fun formatDuration(ms: Long): String {
    if (ms < 0) return "00:00"
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

