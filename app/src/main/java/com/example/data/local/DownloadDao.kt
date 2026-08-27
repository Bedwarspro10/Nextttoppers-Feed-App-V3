package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestampMs DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownloadById(id: String): DownloadEntity?
    
    @Query("SELECT * FROM downloads WHERE lectureId = :lectureId")
    suspend fun getDownloadsByLectureId(lectureId: String): List<DownloadEntity>
    
    @Query("SELECT * FROM downloads WHERE lectureId = :lectureId")
    fun getDownloadsByLectureIdFlow(lectureId: String): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)
    
    @Query("UPDATE downloads SET status = :status, downloadedBytes = :downloadedBytes, totalBytes = :totalBytes, percentage = :percentage, errorMessage = :errorMessage WHERE localMediaId = :localMediaId")
    suspend fun updateDownloadProgress(localMediaId: String, status: Int, downloadedBytes: Long, totalBytes: Long, percentage: Float, errorMessage: String?)
    
    @Query("UPDATE downloads SET status = :status WHERE localMediaId = :localMediaId")
    suspend fun updateDownloadStatus(localMediaId: String, status: Int)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)
    
    @Query("DELETE FROM downloads WHERE localMediaId = :localMediaId")
    suspend fun deleteDownloadByMediaId(localMediaId: String)
}
