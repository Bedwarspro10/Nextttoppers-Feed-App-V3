@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.designsystem.HyperOsMotion
import com.example.data.models.AppUser

// ---------------------------------------------------------------------------------------------
// Avatars
// ---------------------------------------------------------------------------------------------

@Composable
fun ChatAvatar(
    photoUrl: String?,
    name: String,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val displayName = name.ifBlank { "User" }
    val fallbackUrl = "https://ui-avatars.com/api/?name=${displayName}&background=random"
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = photoUrl?.takeIf { it.isNotBlank() } ?: fallbackUrl,
            contentDescription = displayName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Shimmer skeleton (loading state)
// ---------------------------------------------------------------------------------------------

@Composable
private fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val base = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = androidx.compose.ui.geometry.Offset(translate - 300f, 0f),
        end = androidx.compose.ui.geometry.Offset(translate, 300f)
    )
}

@Composable
fun ChatMessagesSkeleton(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val widths = listOf(0.55f, 0.4f, 0.6f, 0.35f, 0.5f)
        widths.forEachIndexed { index, w ->
            val fromUser = index % 2 == 1
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (fromUser) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(w)
                        .height(44.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(brush)
                )
            }
        }
    }
}

@Composable
fun ConversationListSkeleton(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(6) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Empty / error / offline states
// ---------------------------------------------------------------------------------------------

@Composable
fun ChatEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ChatErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun OfflineBanner(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "You're offline — messages will sync when back online",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Date separator
// ---------------------------------------------------------------------------------------------

@Composable
fun DateSeparator(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

// ---------------------------------------------------------------------------------------------
// Message bubble
// ---------------------------------------------------------------------------------------------

val quickReactions = listOf("❤️", "😂", "👍", "😮", "😢")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isFromUser: Boolean,
    isGrouped: Boolean,
    currentUserId: String,
    onLongPress: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val bubbleColor = if (isFromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isFromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isFromUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    // Subtle spring "pop" entrance for a hyper-native feel, on top of LazyColumn's own item animation.
    val scale = remember { Animatable(0.9f) }
    LaunchedEffect(message.id) {
        scale.animateTo(1f, animationSpec = HyperOsMotion.openSpringSpec)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        contentAlignment = if (isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isFromUser) {
                if (!isGrouped) {
                    ChatAvatar(photoUrl = message.senderPhoto, name = message.senderName, size = 32.dp)
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start) {
                if (!isFromUser && !isGrouped && message.senderName.isNotEmpty()) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(if (message.isDeleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else bubbleColor)
                        .then(
                            if (!message.isDeleted) {
                                Modifier.combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onLongPress()
                                    }
                                )
                            } else Modifier
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        message.replyTo?.let { reply ->
                            if (!message.isDeleted) {
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background((if (isFromUser) Color.White else MaterialTheme.colorScheme.surface).copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        reply.senderName.ifBlank { "Message" },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        reply.text,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                        Text(
                            text = if (message.isDeleted) "This message was deleted" else message.text,
                            color = if (message.isDeleted) MaterialTheme.colorScheme.onSurfaceVariant else textColor,
                            fontStyle = if (message.isDeleted) FontStyle.Italic else FontStyle.Normal,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Reactions
                if (message.reactions.isNotEmpty() && !message.isDeleted) {
                    val totalReactions = message.reactions.values.sumOf { it.size }
                    if (totalReactions > 0) {
                        Row(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                message.reactions.entries.joinToString(" ") { it.key },
                                style = MaterialTheme.typography.labelSmall
                            )
                            if (totalReactions > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "$totalReactions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (!message.isDeleted) {
                    Text(
                        text = ChatUtils.messageTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Message action bottom sheet
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionSheet(
    message: ChatMessage,
    isOwn: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    onReact: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                quickReactions.forEach { emoji ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .combinedClickable(onClick = { onReact(emoji); onDismiss() }),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ListItem(
                headlineContent = { Text("Reply") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null) },
                modifier = Modifier.combinedClickable(onClick = { onReply(); onDismiss() })
            )
            ListItem(
                headlineContent = { Text("Copy") },
                leadingContent = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                modifier = Modifier.combinedClickable(onClick = {
                    clipboard.setText(AnnotatedString(message.text))
                    onDismiss()
                })
            )
            if (isOwn) {
                ListItem(
                    headlineContent = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.combinedClickable(onClick = { onDelete(); onDismiss() })
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Reply preview bar (above composer)
// ---------------------------------------------------------------------------------------------

@Composable
fun ReplyPreviewBar(replyTarget: ChatMessage?, onCancel: () -> Unit) {
    AnimatedVisibility(
        visible = replyTarget != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        if (replyTarget != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Replying to ${replyTarget.senderName.ifBlank { "message" }}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        replyTarget.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Composer
// ---------------------------------------------------------------------------------------------

@Composable
fun ChatComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    replyTarget: ChatMessage?,
    onCancelReply: () -> Unit
) {
    val canSend = text.isNotBlank()
    val sendScale by animateFloatAsState(targetValue = if (canSend) 1f else 0.9f, animationSpec = HyperOsMotion.openSpringSpec, label = "sendScale")

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
    ) {
        Column {
            ReplyPreviewBar(replyTarget = replyTarget, onCancel = onCancelReply)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message…") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (canSend) onSend()
                    },
                    enabled = canSend,
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer { scaleX = sendScale; scaleY = sendScale }
                        .background(
                            color = if (canSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Private conversation list item
// ---------------------------------------------------------------------------------------------

@Composable
fun ConversationListItem(
    meta: PrivateChatMeta,
    otherUser: AppUser?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    val displayName = otherUser?.displayName?.takeIf { it.isNotBlank() }
        ?: otherUser?.name?.takeIf { it.isNotBlank() }
        ?: "User"

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChatAvatar(photoUrl = otherUser?.photoURL, name = displayName, size = 52.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = meta.lastMessage.ifBlank { "No messages yet" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ChatUtils.relativeTime(meta.lastMessageTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                AnimatedVisibility(
                    visible = unreadCount > 0,
                    enter = scaleIn(animationSpec = HyperOsMotion.openSpringSpec) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Segmented tab switcher (Community | Private)
// ---------------------------------------------------------------------------------------------

@Composable
fun ChatSegmentedTabs(
    selectedIndex: Int,
    labels: List<String>,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(4.dp)
    ) {
        val segmentWidth = maxWidth / labels.size
        val indicatorOffset by androidx.compose.animation.core.animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = 0.8f,
                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
            ),
            label = "tabIndicator"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .height(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .height(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .combinedClickable(onClick = { onSelected(index) }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
