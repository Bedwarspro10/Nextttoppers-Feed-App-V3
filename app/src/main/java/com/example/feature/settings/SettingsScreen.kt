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
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings") },
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
                        // In a real app we'd use a dropdown menu here
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
                        SettingsDropdownItem("Default playback quality", "Auto", onClick = {})
                        SettingsDropdownItem("Default playback speed", "1.0x", onClick = {})
                        SettingsSwitchItem("Auto-play next lecture", true, onCheckedChange = {})
                    }
                }
                item {
                    SettingsSection("DOWNLOADS") {
                        SettingsDropdownItem("Download quality preference", "High (1080p)", onClick = {})
                        SettingsSwitchItem("Download over Wi-Fi only", true, onCheckedChange = {})
                    }
                }
                item {
                    SettingsSection("NOTIFICATIONS") {
                        SettingsSwitchItem("Announcements notifications", true, onCheckedChange = {})
                        SettingsSwitchItem("Chat notifications", true, onCheckedChange = {})
                    }
                }
                item {
                    SettingsSection("DATA & STORAGE") {
                        SettingsActionItem("Storage usage", "240 MB Cache • 1.2 GB Downloads", onClick = {})
                        SettingsActionItem("Clear cache", "Frees up space without deleting downloads", onClick = {})
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
                        val context = LocalContext.current
                        val coroutineScope = rememberCoroutineScope()
                        SettingsActionItem("Export APK", "Save this app's APK to Downloads folder", onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                exportApk(context)
                            }
                        })
                    }
                }
                item {
                    SettingsSection("ABOUT") {
                        SettingsActionItem("App version", "1.0.0 (Build 1)", onClick = {})
                        SettingsActionItem("Privacy Policy", "", onClick = {})
                        SettingsActionItem("Terms of Service", "", onClick = {})
                        SettingsActionItem("Contact Us", "support@nexttoppers.com", onClick = {})
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
            shape = RoundedCornerShape(16.dp),
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
