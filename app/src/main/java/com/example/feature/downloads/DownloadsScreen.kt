package com.example.feature.downloads

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.offline.Download
import com.example.data.local.DownloadEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel,
    onNavigateBack: () -> Unit,
    onPlayVideo: (String, String, String) -> Unit // url, title, localMediaId
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.downloads.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Downloads Yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val activeDownloads = uiState.downloads.filter { it.status != Download.STATE_COMPLETED && it.status != Download.STATE_FAILED }
                val completedDownloads = uiState.downloads.filter { it.status == Download.STATE_COMPLETED }
                val failedDownloads = uiState.downloads.filter { it.status == Download.STATE_FAILED }

                if (activeDownloads.isNotEmpty()) {
                    item {
                        Text("Downloading", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(activeDownloads) { download ->
                        DownloadItem(
                            download = download,
                            onClick = { },
                            onPause = { viewModel.pauseDownload(download.id) },
                            onResume = { viewModel.resumeDownload(download.id) },
                            onCancel = { viewModel.removeDownload(download.id) }
                        )
                    }
                }

                if (completedDownloads.isNotEmpty()) {
                    item {
                        Text("Completed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(completedDownloads) { download ->
                        DownloadItem(
                            download = download,
                            onClick = { 
                                onPlayVideo(download.url, download.title, download.localMediaId) 
                            },
                            onPause = null,
                            onResume = null,
                            onCancel = { viewModel.removeDownload(download.id) }
                        )
                    }
                }
                
                if (failedDownloads.isNotEmpty()) {
                    item {
                        Text("Failed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(failedDownloads) { download ->
                        DownloadItem(
                            download = download,
                            onClick = { },
                            onPause = null,
                            onResume = { viewModel.resumeDownload(download.id) },
                            onCancel = { viewModel.removeDownload(download.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItem(
    download: DownloadEntity,
    onClick: () -> Unit,
    onPause: (() -> Unit)?,
    onResume: (() -> Unit)?,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${download.subject} • ${download.selectedQuality}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                if (download.status != Download.STATE_COMPLETED) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { if (download.percentage >= 0) download.percentage / 100f else 0f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val statusText = when(download.status) {
                        Download.STATE_QUEUED -> "Queued"
                        Download.STATE_STOPPED -> "Paused"
                        Download.STATE_DOWNLOADING -> "Downloading... ${download.percentage.toInt()}%"
                        Download.STATE_FAILED -> "Failed: ${download.errorMessage ?: "Unknown error"}"
                        else -> "Processing..."
                    }
                    
                    val sizeText = if (download.totalBytes > 0) {
                        "${download.downloadedBytes / (1024 * 1024)}MB / ${download.totalBytes / (1024 * 1024)}MB"
                    } else {
                        "${download.downloadedBytes / (1024 * 1024)}MB"
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(statusText, style = MaterialTheme.typography.labelSmall)
                        Text(sizeText, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    val sizeText = if (download.totalBytes > 0) "${download.totalBytes / (1024 * 1024)}MB" else ""
                    if (sizeText.isNotEmpty()) {
                        Text(
                            text = sizeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))

            if (download.status == Download.STATE_DOWNLOADING && onPause != null) {
                IconButton(onClick = onPause) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                }
            } else if ((download.status == Download.STATE_STOPPED || download.status == Download.STATE_FAILED) && onResume != null) {
                IconButton(onClick = onResume) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                }
            }

            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
