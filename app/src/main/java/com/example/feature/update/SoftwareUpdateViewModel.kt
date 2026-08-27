package com.example.feature.update

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed interface DownloadEvent {
    object Started : DownloadEvent
    data class Progress(val percent: Int) : DownloadEvent
    object Completed : DownloadEvent
    data class Failed(val message: String) : DownloadEvent
}

class SoftwareUpdateViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val _downloadEvent = MutableSharedFlow<DownloadEvent>()
    val downloadEvent = _downloadEvent.asSharedFlow()

    private var downloadJob: kotlinx.coroutines.Job? = null
    private var isCancelled = false

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    fun getAppVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    fun downloadUpdate(context: Context, urlString: String) {
        // Validate URL before starting the download
        val isAllowedHost = urlString.startsWith("https://github.com/") || 
                            urlString.startsWith("https://nexttopper-feed.pages.dev/") || 
                            urlString.startsWith("https://nexttopper-feed-chat-site.pages.dev/")

        if (!isAllowedHost) {
            Log.e("SoftwareUpdate", "Blocked attempt to download from unauthorized URL: $urlString")
            viewModelScope.launch {
                _downloadEvent.emit(DownloadEvent.Failed("Blocked attempt to download from unauthorized URL."))
            }
            return
        }

        isCancelled = false
        downloadJob?.cancel() // Cancel any previous running download job

        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("SoftwareUpdate", "Starting download: $urlString")
                _downloadEvent.emit(DownloadEvent.Started)

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server returned HTTP error ${connection.responseCode}")
                }

                val totalBytes = connection.contentLength
                val inputStream = BufferedInputStream(connection.inputStream)
                
                // Save to cache dir so it is shareable via FileProvider
                val apkFile = File(context.externalCacheDir ?: context.cacheDir, "app-update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }
                
                val outputStream = FileOutputStream(apkFile)
                val buffer = ByteArray(1024 * 8)
                var bytesRead: Int
                var totalBytesRead = 0L
                var lastProgressUpdate = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (isCancelled) {
                        outputStream.close()
                        inputStream.close()
                        connection.disconnect()
                        try {
                            apkFile.delete()
                        } catch (e: Exception) {}
                        Log.d("SoftwareUpdate", "Download cancelled by user")
                        return@launch
                    }
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    val now = System.currentTimeMillis()
                    // Update JS progress every 100ms or when complete to prevent bridge congestion
                    if (now - lastProgressUpdate > 100 || totalBytesRead == totalBytes.toLong()) {
                        lastProgressUpdate = now
                        val progress = if (totalBytes > 0) (totalBytesRead * 100 / totalBytes).toInt() else 0
                        _downloadEvent.emit(DownloadEvent.Progress(progress))
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                connection.disconnect()

                Log.d("SoftwareUpdate", "Download finished successfully!")
                _downloadEvent.emit(DownloadEvent.Completed)

                // Trigger Android System Installer handoff on UI thread
                withContext(Dispatchers.Main) {
                    installApk(context, apkFile)
                }

            } catch (e: Exception) {
                Log.e("SoftwareUpdate", "Download failed", e)
                _downloadEvent.emit(DownloadEvent.Failed(e.localizedMessage ?: "Unknown network error"))
            }
        }
    }

    fun cancelDownload() {
        isCancelled = true
        downloadJob?.cancel()
    }

    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        val authority = "${context.packageName}.fileprovider"
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, apkFile)

            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // For Android 8.0 and above, verify "Install Unknown Apps" source permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Log.d("SoftwareUpdate", "Requesting Install Unknown Apps permission from settings")
                    val settingsIntent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SoftwareUpdate", "Failed to invoke package installer", e)
            android.widget.Toast.makeText(context, "Installation failed: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SoftwareUpdateViewModel(appContainer) as T
                }
            }
    }
}
