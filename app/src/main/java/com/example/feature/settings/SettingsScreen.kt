package com.example.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSoftwareUpdate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Text("Next Toppers values your privacy. We store your data locally on your device via Room Database and only synchronize course progress with Firebase secure servers. No personal information is ever sold or shared with third parties.")
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
            text = {
                Text("By using Next Toppers, you agree to access course material for your personal educational use only. Distribution or reproduction of any platform material is strictly prohibited. Your day streak is updated daily based on activity.")
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Contact Support", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Email: support@nexttoppers.com", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Response time: Under 24 hours. Feel free to reach out with any questions about courses, ranks, or technical support.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:support@nexttoppers.com")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Next Toppers Support")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No email app found. Please mail support@nexttoppers.com", Toast.LENGTH_LONG).show()
                    }
                    showContactDialog = false
                }) {
                    Text("Send Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SettingsSection("APPEARANCE") {
                        SettingsDropdownItem(
                            title = "Theme",
                            value = uiState.theme,
                            onClick = {
                                val nextTheme = when (uiState.theme) {
                                    "System Default" -> "Light"
                                    "Light" -> "Dark"
                                    else -> "System Default"
                                }
                                viewModel.setTheme(nextTheme)
                            }
                        )
                    }
                }
                item {
                    SettingsSection("VIDEO") {
                        SettingsDropdownItem(
                            title = "Default playback quality",
                            value = uiState.playbackQuality,
                            onClick = {
                                val nextQuality = when (uiState.playbackQuality) {
                                    "Auto" -> "360p"
                                    "360p" -> "480p"
                                    "480p" -> "720p"
                                    "720p" -> "1080p"
                                    else -> "Auto"
                                }
                                viewModel.setPlaybackQuality(nextQuality)
                                Toast.makeText(context, "Playback quality updated to $nextQuality", Toast.LENGTH_SHORT).show()
                            }
                        )
                        SettingsDropdownItem(
                            title = "Default playback speed",
                            value = uiState.playbackSpeed,
                            onClick = {
                                val nextSpeed = when (uiState.playbackSpeed) {
                                    "1.0x" -> "1.25x"
                                    "1.25x" -> "1.5x"
                                    "1.5x" -> "2.0x"
                                    "2.0x" -> "0.75x"
                                    "0.75x" -> "0.5x"
                                    else -> "1.0x"
                                }
                                viewModel.setPlaybackSpeed(nextSpeed)
                                Toast.makeText(context, "Playback speed set to $nextSpeed", Toast.LENGTH_SHORT).show()
                            }
                        )
                        SettingsSwitchItem(
                            title = "Auto-play next lecture",
                            checked = uiState.autoplayNext,
                            onCheckedChange = {
                                viewModel.setAutoplayNext(it)
                                Toast.makeText(context, if (it) "Autoplay enabled" else "Autoplay disabled", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
                item {
                    SettingsSection("DOWNLOADS") {
                        SettingsDropdownItem(
                            title = "Download quality preference",
                            value = uiState.downloadQuality,
                            onClick = {
                                val nextQuality = when (uiState.downloadQuality) {
                                    "High (1080p)" -> "Medium (720p)"
                                    "Medium (720p)" -> "Low (480p)"
                                    else -> "High (1080p)"
                                }
                                viewModel.setDownloadQuality(nextQuality)
                                Toast.makeText(context, "Download quality set to $nextQuality", Toast.LENGTH_SHORT).show()
                            }
                        )
                        SettingsSwitchItem(
                            title = "Download over Wi-Fi only",
                            checked = uiState.downloadWifiOnly,
                            onCheckedChange = {
                                viewModel.setDownloadWifiOnly(it)
                                Toast.makeText(context, if (it) "Downloads restricted to Wi-Fi" else "Downloads allowed over Mobile Data", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
                item {
                    SettingsSection("NOTIFICATIONS") {
                        SettingsSwitchItem(
                            title = "Announcements notifications",
                            checked = uiState.announcementsNotif,
                            onCheckedChange = {
                                viewModel.setAnnouncementsNotif(it)
                            }
                        )
                        SettingsSwitchItem(
                            title = "Chat notifications",
                            checked = uiState.chatNotif,
                            onCheckedChange = {
                                viewModel.setChatNotif(it)
                            }
                        )
                    }
                }
                item {
                    SettingsSection("DATA & STORAGE") {
                        var cacheSize by remember { mutableStateOf("240 MB Cache • 1.2 GB Downloads") }
                        SettingsActionItem(
                            title = "Storage usage",
                            subtitle = cacheSize,
                            onClick = {
                                Toast.makeText(context, "Storage: $cacheSize", Toast.LENGTH_SHORT).show()
                            }
                        )
                        SettingsActionItem(
                            title = "Clear cache",
                            subtitle = "Frees up space without deleting downloads",
                            onClick = {
                                try {
                                    context.cacheDir.deleteRecursively()
                                    cacheSize = "0 MB Cache • 1.2 GB Downloads"
                                    Toast.makeText(context, "Cache successfully cleared!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to clear cache", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
                item {
                    SettingsSection("ACCESSIBILITY") {
                        SettingsSwitchItem(
                            title = "Reduce motion", 
                            checked = uiState.reduceMotion,
                            onCheckedChange = { viewModel.setReduceMotion(it) }
                        )
                    }
                }
                item {
                    SettingsSection("APP") {
                        SettingsActionItem("Export APK", "Save this app's APK to Downloads folder", onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                exportApk(context)
                            }
                        })
                    }
                }
                item {
                    SettingsSection("ABOUT") {
                        SettingsActionItem("App version", "1.0.0 (Build 1)", onClick = onNavigateToSoftwareUpdate)
                        SettingsActionItem("Software update", "Check for system updates", onClick = onNavigateToSoftwareUpdate)
                        SettingsActionItem("Privacy Policy", "Read our terms of data handling", onClick = {
                            showPrivacyDialog = true
                        })
                        SettingsActionItem("Terms of Service", "Read our terms of use", onClick = {
                            showTermsDialog = true
                        })
                        SettingsActionItem("Contact Us", "support@nexttoppers.com", onClick = {
                            showContactDialog = true
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun SettingsDropdownItem(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun SettingsSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsActionItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

private suspend fun exportApk(context: Context) {
    withContext(Dispatchers.IO) {
        try {
            val applicationInfo = context.applicationInfo
            val apkFile = File(applicationInfo.sourceDir)
            val fileName = "NextToppersFeed_Export.apk"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/vnd.android.package-archive")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        FileInputStream(apkFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "APK exported to Downloads", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, fileName)
                FileInputStream(apkFile).use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "APK exported to Downloads", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to export APK", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
