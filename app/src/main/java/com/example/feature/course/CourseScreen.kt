package com.example.feature.course

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.GlobalQuickMenu
import com.example.data.models.ContentNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    viewModel: CourseViewModel,
    onNavigateBack: () -> Unit,
    onPlayVideo: (String, String, String, String) -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    BackHandler {
        if (!viewModel.navigateBack()) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val currentFolder = uiState.navigationStack.lastOrNull()?.document?.title
                    Text(currentFolder ?: "Course Content", maxLines = 1)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!viewModel.navigateBack()) {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    GlobalQuickMenu(
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToDownloads = onNavigateToDownloads
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                com.example.core.designsystem.HyperOsMotion.enterTransition togetherWith
                com.example.core.designsystem.HyperOsMotion.exitTransition
            },
            label = "CourseScreenContent",
            modifier = Modifier.padding(padding)
        ) { state ->
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                state.currentNodes.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No content found.")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.currentNodes) { node ->
                            ContentNodeItem(
                                node = node,
                                onClick = {
                                    if (node.isFolder) {
                                        viewModel.openFolder(node)
                                    } else {
                                        if (!node.isResolved) {
                                            Toast.makeText(context, "This content is pending and not yet available.", Toast.LENGTH_SHORT).show()
                                            return@ContentNodeItem
                                        }
                                        
                                        if (node.isPremiumLocked) {
                                            Toast.makeText(context, "This content is locked for Premium users.", Toast.LENGTH_SHORT).show()
                                            return@ContentNodeItem
                                        }

                                        val url = node.document.resolvedUrl
                                        if (url.isNullOrEmpty()) {
                                            Toast.makeText(context, "No URL available for this content.", Toast.LENGTH_SHORT).show()
                                            return@ContentNodeItem
                                        }

                                        val isLecture = url.contains(".m3u8") || url.contains(".mp4") || url.contains("youtube")
                                        if (isLecture) {
                                            onPlayVideo(url, node.document.title, node.document.entityId, node.document.courseId)
                                        } else {
                                            // PDF or unknown, fallback to intent
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContentNodeItem(
    node: ContentNode,
    onClick: () -> Unit
) {
    val isPending = !node.isFolder && !node.isResolved

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPending) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val url = node.document.resolvedUrl ?: ""
            val isLecture = url.contains(".m3u8") || url.contains(".mp4") || url.contains("youtube")
            
            Box(modifier = Modifier.size(48.dp)) {
                when {
                    node.isFolder -> com.example.core.designsystem.FolderIcon3D(modifier = Modifier.fillMaxSize())
                    isLecture -> com.example.core.designsystem.VideoIcon3D(modifier = Modifier.fillMaxSize())
                    else -> com.example.core.designsystem.PdfIcon3D(modifier = Modifier.fillMaxSize())
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPending) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                if (!node.isFolder) {
                    Text(
                        text = if (isPending) "Pending..." else if (isLecture) "Lecture" else "Document",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPending) Color.Gray else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            if (node.isPremiumLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Premium Content",
                    tint = Color(0xFFF59E0B) // Amber
                )
            } else if (node.isFolder) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open Folder",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
