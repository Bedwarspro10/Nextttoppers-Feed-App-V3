package com.example.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage

@Composable
fun GlobalQuickMenu(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    userPhotoUrl: String? = null
) {
    var showPopup by remember { mutableStateOf(false) }
    val visibleState = remember { MutableTransitionState(false) }
    
    var buttonSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val density = LocalDensity.current

    Box {
        IconButton(
            onClick = { 
                showPopup = true
                visibleState.targetState = true
            },
            modifier = Modifier.onGloballyPositioned { coordinates ->
                buttonSize = coordinates.size
            }
        ) {
            Icon(Icons.Default.MoreHoriz, contentDescription = "Quick Menu")
        }

        if (showPopup) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(
                    x = 0,
                    y = buttonSize.height + with(density) { 8.dp.roundToPx() }
                ),
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                ),
                onDismissRequest = { visibleState.targetState = false }
            ) {
                LaunchedEffect(visibleState.currentState, visibleState.targetState) {
                    if (!visibleState.targetState && !visibleState.currentState) {
                        showPopup = false
                    }
                }

                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = fadeIn(tween(200, easing = FastOutSlowInEasing)) + 
                            scaleIn(
                                initialScale = 0.92f, 
                                animationSpec = tween(200, easing = FastOutSlowInEasing),
                                transformOrigin = TransformOrigin(1f, 0f)
                            ) +
                            slideInVertically(
                                initialOffsetY = { -it / 20 },
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ),
                    exit = fadeOut(tween(180, easing = FastOutSlowInEasing)) + 
                           scaleOut(
                               targetScale = 0.92f, 
                               animationSpec = tween(180, easing = FastOutSlowInEasing),
                               transformOrigin = TransformOrigin(1f, 0f)
                           ) +
                           slideOutVertically(
                               targetOffsetY = { -it / 20 },
                               animationSpec = tween(180, easing = FastOutSlowInEasing)
                           )
                ) {
                    // Liquid Glass Menu Surface
                    Surface(
                        modifier = Modifier
                            .width(220.dp)
                            .padding(end = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shadowElevation = 8.dp,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(8.dp)
                        ) {
                            QuickMenuItem(
                                icon = Icons.Default.Person,
                                label = "Profile",
                                photoUrl = userPhotoUrl,
                                onClick = {
                                    visibleState.targetState = false
                                    onNavigateToProfile()
                                }
                            )
                            QuickMenuItem(
                                icon = Icons.Default.Settings,
                                label = "App Settings",
                                onClick = {
                                    visibleState.targetState = false
                                    onNavigateToSettings()
                                }
                            )
                            QuickMenuItem(
                                icon = Icons.Default.Download,
                                label = "Downloads",
                                onClick = {
                                    visibleState.targetState = false
                                    onNavigateToDownloads()
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
private fun QuickMenuItem(
    icon: ImageVector,
    label: String,
    photoUrl: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (photoUrl != null && label == "Profile") {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
