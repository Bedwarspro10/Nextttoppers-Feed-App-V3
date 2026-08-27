package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.core.di.AppContainer
import com.example.core.di.DefaultAppContainer
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBdTbN0nNiR5nm2IIDl4xgS36e3AbKBmGs")
                .setApplicationId("1:42465208642:web:1b3629672d546c501c40db")
                .setProjectId("aarambh26-27")
                .setStorageBucket("aarambh26-27.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(this, options)
        }
        
        createNotificationChannel()
        container = DefaultAppContainer(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "next_toppers_feed_channel",
                "General Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for chat and updates"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
