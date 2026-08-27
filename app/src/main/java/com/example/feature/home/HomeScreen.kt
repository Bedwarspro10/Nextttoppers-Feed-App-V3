package com.example.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.spring

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.models.Announcement
import com.example.data.models.Banner
import com.example.data.models.Subject
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import com.example.core.designsystem.GlobalQuickMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSubject: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState.isLoading,
        transitionSpec = {
            com.example.core.designsystem.HyperOsMotion.enterTransition togetherWith
            com.example.core.designsystem.HyperOsMotion.exitTransition
        },
        label = "HomeScreenContent"
    ) { isLoading ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Header Section (Profile + Stats)
                item {
                    HeaderSection(
                        name = uiState.user?.displayName ?: "Student",
                        photoUrl = uiState.user?.photoURL,
                        level = uiState.user?.level ?: 1,
                        streak = uiState.user?.streak ?: 0,
                        coins = uiState.wallet?.balance ?: 0,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToDownloads = onNavigateToDownloads,
                        onNavigateToWallet = onNavigateToWallet
                    )
                }

                // 2. Banners Section
                if (uiState.banners.isNotEmpty()) {
                    item {
                        BannersSection(banners = uiState.banners)
                    }
                }

                // 3. Announcements Section
                if (uiState.announcements.isNotEmpty()) {
                    item {
                        AnnouncementsSection(announcements = uiState.announcements)
                    }
                }

                // 4. Subjects Grid (or list)
                if (uiState.subjects.isNotEmpty()) {
                    item {
                        Text(
                            text = "Your Subjects",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(uiState.subjects.chunked(2)) { rowSubjects ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (subject in rowSubjects) {
                                SubjectCard(
                                    subject = subject,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onNavigateToSubject(subject.id) }
                                )
                            }
                            // Handle odd number of items by adding an empty spacer
                            if (rowSubjects.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    name: String,
    photoUrl: String?,
    level: Int,
    streak: Int,
    coins: Int,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (photoUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Hi, $name! 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Level $level",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        GlobalQuickMenu(
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToDownloads = onNavigateToDownloads,
            userPhotoUrl = photoUrl
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Stats Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = { com.example.core.designsystem.FlameIcon3D(modifier = Modifier.fillMaxSize()) },
            label = "Day Streak",
            value = streak.toString()
        )
        StatCard(
            modifier = Modifier.weight(1f).clickable { onNavigateToWallet() },
            icon = { com.example.core.designsystem.CoinIcon3D(modifier = Modifier.fillMaxSize()) },
            label = "NT Coins",
            value = coins.toString()
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp)) {
                icon()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun BannersSection(banners: List<Banner>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(banners) { banner ->
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(140.dp)
                    .clickable { /* Handle click URL if needed */ },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .align(Alignment.CenterStart)
                    ) {
                        Text(
                            text = banner.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = banner.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementsSection(announcements: List<Announcement>) {
    Text(
        text = "Announcements",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            announcements.forEachIndexed { index, announcement ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(32.dp).padding(top = 2.dp)) {
                        com.example.core.designsystem.MegaphoneIcon3D(modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = announcement.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = announcement.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (index < announcements.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 400f)
    )
    
    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconRotation by animateFloatAsState(
                targetValue = if (isPressed) -8f else 0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
            )
            Box(modifier = Modifier.size(56.dp).graphicsLayer(rotationZ = iconRotation)) {
                when (subject.name.lowercase()) {
                    "mathematics", "maths", "math" -> com.example.core.designsystem.MathIcon3D(modifier = Modifier.fillMaxSize())
                    "science" -> com.example.core.designsystem.ScienceIcon3D(modifier = Modifier.fillMaxSize())
                    "social science", "sst" -> com.example.core.designsystem.GlobeIcon3D(modifier = Modifier.fillMaxSize())
                    "english" -> com.example.core.designsystem.EnglishIcon3D(modifier = Modifier.fillMaxSize())
                    "hindi" -> com.example.core.designsystem.HindiIcon3D(modifier = Modifier.fillMaxSize())
                    "notes", "document" -> com.example.core.designsystem.NotesIcon3D(modifier = Modifier.fillMaxSize())
                    else -> com.example.core.designsystem.NotesIcon3D(modifier = Modifier.fillMaxSize())
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Explore chapters",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
