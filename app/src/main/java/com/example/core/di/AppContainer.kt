package com.example.core.di

import android.content.Context
import com.example.core.download.DownloadRepository
import com.example.core.network.ConnectivityRepository
import com.example.data.local.AppDatabase
import androidx.media3.common.util.UnstableApi
import com.example.core.datastore.LocalPreferences
import com.example.core.network.ApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

interface AppContainer {
    val firebaseAuth: FirebaseAuth
    val firestore: FirebaseFirestore
    val apiService: ApiService
    val localPreferences: LocalPreferences
    val geminiApiService: com.example.core.network.GeminiApiService
    val firebaseStorage: FirebaseStorage
    val downloadRepository: DownloadRepository
    val connectivityRepository: ConnectivityRepository

}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    override val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    override val firebaseStorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    
    @androidx.annotation.OptIn(UnstableApi::class)
    override val downloadRepository: DownloadRepository by lazy { 
        DownloadRepository(context, AppDatabase.getDatabase(context).downloadDao()) 
    }
    
    override val connectivityRepository: ConnectivityRepository by lazy {
        ConnectivityRepository(context)
    }

    override val localPreferences: LocalPreferences by lazy { LocalPreferences(context) }

    // Hardcoded Base URL for the Express API server (same domain as web app).
    // In a real scenario, this would come from BuildConfig or Secrets.
    private val baseUrl = "https://ais-dev-egcwoibsb254w6uae7lbdr-465025689742.asia-east1.run.app/api/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    override val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    private val geminiOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    private val geminiRetrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(geminiOkHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    override val geminiApiService: com.example.core.network.GeminiApiService by lazy {
        geminiRetrofit.create(com.example.core.network.GeminiApiService::class.java)
    }
}
