package com.example.core.download

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import com.example.data.local.DownloadDao
import com.example.data.local.DownloadEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@UnstableApi
class DownloadRepository(
    private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val downloadManager: DownloadManager = DownloadUtil.getDownloadManager(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                scope.launch {
                    val entity = downloadDao.getDownloadById(download.request.id) ?: return@launch
                    val status = download.state
                    val downloadedBytes = download.bytesDownloaded
                    val totalBytes = download.contentLength
                    val percentage = download.percentDownloaded
                    val errorMsg = finalException?.message
                    
                    downloadDao.updateDownloadProgress(
                        localMediaId = download.request.id,
                        status = status,
                        downloadedBytes = downloadedBytes,
                        totalBytes = totalBytes,
                        percentage = percentage,
                        errorMessage = errorMsg
                    )
                }
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                scope.launch {
                    downloadDao.deleteDownloadByMediaId(download.request.id)
                }
            }
        })
    }

    suspend fun getAvailableQualities(url: String): List<String> {
        return try {
            HlsDownloadHelper.getAvailableQualities(context, url)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addDownload(
        lectureId: String,
        title: String,
        subject: String,
        url: String,
        quality: String
    ) {
        val uniqueId = "${lectureId}_$quality"
        
        // Prevent duplicate download
        val existing = downloadDao.getDownloadById(uniqueId)
        if (existing != null) return
        
        val request = try {
            HlsDownloadHelper.createDownloadRequest(context, url, quality, uniqueId)
        } catch (e: Exception) {
            // Fallback to basic request if HLS parsing fails
            androidx.media3.exoplayer.offline.DownloadRequest.Builder(uniqueId, android.net.Uri.parse(url)).build()
        }
            
        val entity = DownloadEntity(
            id = uniqueId,
            lectureId = lectureId,
            title = title,
            subject = subject,
            url = url,
            selectedQuality = quality,
            status = Download.STATE_QUEUED,
            downloadedBytes = 0L,
            totalBytes = -1L,
            percentage = 0f,
            localMediaId = uniqueId,
            timestampMs = System.currentTimeMillis()
        )
        downloadDao.insertDownload(entity)
        DownloadService.sendAddDownload(
            context,
            VideoDownloadService::class.java,
            request,
            true
        )
    }

    fun pauseDownload(id: String) {
        DownloadService.sendSetStopReason(
            context,
            VideoDownloadService::class.java,
            id,
            Download.STOP_REASON_NONE,
            true
        )
    }

    fun resumeDownload(id: String) {
        DownloadService.sendSetStopReason(
            context,
            VideoDownloadService::class.java,
            id,
            Download.STOP_REASON_NONE,
            false
        )
    }

    fun removeDownload(id: String) {
        DownloadService.sendRemoveDownload(
            context,
            VideoDownloadService::class.java,
            id,
            false
        )
    }

    fun getAllDownloads(): Flow<List<DownloadEntity>> {
        return downloadDao.getAllDownloads()
    }
    
    fun getDownloadsForLecture(lectureId: String): Flow<List<DownloadEntity>> {
        return downloadDao.getDownloadsByLectureIdFlow(lectureId)
    }
}
