package com.example.core.download

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executor

@UnstableApi
object DownloadUtil {
    private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

    private var databaseProvider: DatabaseProvider? = null
    private var downloadCache: Cache? = null
    private var downloadManager: DownloadManager? = null

    @Synchronized
    fun getDatabaseProvider(context: Context): DatabaseProvider {
        if (databaseProvider == null) {
            databaseProvider = StandaloneDatabaseProvider(context)
        }
        return databaseProvider!!
    }

    @Synchronized
    fun getDownloadCache(context: Context): Cache {
        if (downloadCache == null) {
            val downloadContentDirectory = File(context.filesDir, DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(
                downloadContentDirectory,
                NoOpCacheEvictor(),
                getDatabaseProvider(context)
            )
        }
        return downloadCache!!
    }

    @Synchronized
    fun getDownloadManager(context: Context): DownloadManager {
        if (downloadManager == null) {
            val executor = Executor { command -> Thread(command).start() }
            val downloadIndex = androidx.media3.exoplayer.offline.DefaultDownloadIndex(getDatabaseProvider(context))
            
            // We use DefaultHttpDataSource for downloading
            val dataSourceFactory = DefaultHttpDataSource.Factory()
            
            downloadManager = DownloadManager(
                context,
                getDatabaseProvider(context),
                getDownloadCache(context),
                dataSourceFactory,
                executor
            ).apply {
                maxParallelDownloads = 3
            }
        }
        return downloadManager!!
    }
    
    @Synchronized
    fun getDataSourceFactory(context: Context): DataSource.Factory {
        return DefaultHttpDataSource.Factory()
    }
    
    @Synchronized
    fun getCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        val cache = getDownloadCache(context)
        val upstreamFactory = DefaultHttpDataSource.Factory()
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null) // Disable writing when playing offline
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}
