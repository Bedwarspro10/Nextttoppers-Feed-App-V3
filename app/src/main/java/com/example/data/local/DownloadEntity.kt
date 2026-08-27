package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val lectureId: String,
    val title: String,
    val subject: String,
    val url: String,
    val selectedQuality: String,
    val status: Int, // e.g. 0=Queued, 1=Downloading, 2=Completed, 3=Failed, 4=Paused
    val downloadedBytes: Long,
    val totalBytes: Long,
    val percentage: Float,
    val localMediaId: String, // Maps to Media3 Download.request.id
    val timestampMs: Long,
    val errorMessage: String? = null
)
