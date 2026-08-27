package com.example.core.download

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@UnstableApi
object HlsDownloadHelper {

    suspend fun getAvailableQualities(context: Context, url: String): List<String> {
        val helper = DownloadHelper.forMediaItem(
            context,
            MediaItem.fromUri(Uri.parse(url)),
            androidx.media3.exoplayer.DefaultRenderersFactory(context),
            DownloadUtil.getDataSourceFactory(context)
        )
        
        return suspendCancellableCoroutine { continuation ->
            helper.prepare(object : DownloadHelper.Callback {
                override fun onPrepared(helper: DownloadHelper) {
                    val qualities = mutableSetOf<String>()
                    for (periodIndex in 0 until helper.periodCount) {
                        val trackGroups = helper.getTrackGroups(periodIndex)
                        for (i in 0 until trackGroups.length) {
                            val trackGroup = trackGroups.get(i)
                            for (j in 0 until trackGroup.length) {
                                val format = trackGroup.getFormat(j)
                                if (format.height > 0) {
                                    qualities.add("${format.height}p")
                                }
                            }
                        }
                    }
                    helper.release()
                    continuation.resume(qualities.sortedByDescending { it.replace("p", "").toIntOrNull() ?: 0 })
                }

                override fun onPrepareError(helper: DownloadHelper, e: IOException) {
                    helper.release()
                    continuation.resumeWithException(e)
                }
            })
        }
    }

    suspend fun createDownloadRequest(
        context: Context,
        url: String,
        qualityId: String,
        uniqueId: String
    ): DownloadRequest {
        val targetHeight = qualityId.replace("p", "").toIntOrNull() ?: -1
        
        val helper = DownloadHelper.forMediaItem(
            context,
            MediaItem.fromUri(Uri.parse(url)),
            androidx.media3.exoplayer.DefaultRenderersFactory(context),
            DownloadUtil.getDataSourceFactory(context)
        )

        return suspendCancellableCoroutine { continuation ->
            helper.prepare(object : DownloadHelper.Callback {
                override fun onPrepared(helper: DownloadHelper) {
                    if (targetHeight > 0) {
                        val trackSelectionParameters = TrackSelectionParameters.Builder(context)
                            .setMaxVideoSize(Int.MAX_VALUE, targetHeight)
                            .setMinVideoSize(0, targetHeight)
                            .build()
                            
                        for (periodIndex in 0 until helper.periodCount) {
                            helper.clearTrackSelections(periodIndex)
                            helper.addTrackSelection(periodIndex, trackSelectionParameters)
                        }
                    }
                    
                    val request = helper.getDownloadRequest(uniqueId, null)
                    helper.release()
                    continuation.resume(request)
                }

                override fun onPrepareError(helper: DownloadHelper, e: IOException) {
                    helper.release()
                    continuation.resumeWithException(e)
                }
            })
        }
    }
}
