package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "next_toppers_prefs")

class LocalPreferences(private val context: Context) {

    private val THEME_KEY = stringPreferencesKey("nt_theme")
    private val REDUCE_MOTION_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("nt_reduce_motion")
    
    // Video settings
    private val PLAYBACK_QUALITY_KEY = stringPreferencesKey("nt_playback_quality")
    private val PLAYBACK_SPEED_KEY = stringPreferencesKey("nt_playback_speed")
    private val AUTOPLAY_NEXT_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("nt_autoplay_next")
    
    // Download settings
    private val DOWNLOAD_QUALITY_KEY = stringPreferencesKey("nt_download_quality")
    private val DOWNLOAD_WIFI_ONLY_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("nt_download_wifi_only")
    
    // Notification settings
    private val ANNOUNCEMENTS_NOTIF_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("nt_announcements_notif")
    private val CHAT_NOTIF_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("nt_chat_notif")
    
    fun getTheme(): Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "System Default" }
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs -> prefs[THEME_KEY] = theme }
    }
    
    fun getReduceMotion(): Flow<Boolean> = context.dataStore.data.map { it[REDUCE_MOTION_KEY] ?: false }
    suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[REDUCE_MOTION_KEY] = enabled }
    }

    fun getPlaybackQuality(): Flow<String> = context.dataStore.data.map { it[PLAYBACK_QUALITY_KEY] ?: "Auto" }
    suspend fun setPlaybackQuality(quality: String) {
        context.dataStore.edit { prefs -> prefs[PLAYBACK_QUALITY_KEY] = quality }
    }

    fun getPlaybackSpeed(): Flow<String> = context.dataStore.data.map { it[PLAYBACK_SPEED_KEY] ?: "1.0x" }
    suspend fun setPlaybackSpeed(speed: String) {
        context.dataStore.edit { prefs -> prefs[PLAYBACK_SPEED_KEY] = speed }
    }

    fun getAutoplayNext(): Flow<Boolean> = context.dataStore.data.map { it[AUTOPLAY_NEXT_KEY] ?: true }
    suspend fun setAutoplayNext(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[AUTOPLAY_NEXT_KEY] = enabled }
    }

    fun getDownloadQuality(): Flow<String> = context.dataStore.data.map { it[DOWNLOAD_QUALITY_KEY] ?: "High (1080p)" }
    suspend fun setDownloadQuality(quality: String) {
        context.dataStore.edit { prefs -> prefs[DOWNLOAD_QUALITY_KEY] = quality }
    }

    fun getDownloadWifiOnly(): Flow<Boolean> = context.dataStore.data.map { it[DOWNLOAD_WIFI_ONLY_KEY] ?: true }
    suspend fun setDownloadWifiOnly(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[DOWNLOAD_WIFI_ONLY_KEY] = enabled }
    }

    fun getAnnouncementsNotif(): Flow<Boolean> = context.dataStore.data.map { it[ANNOUNCEMENTS_NOTIF_KEY] ?: true }
    suspend fun setAnnouncementsNotif(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[ANNOUNCEMENTS_NOTIF_KEY] = enabled }
    }

    fun getChatNotif(): Flow<Boolean> = context.dataStore.data.map { it[CHAT_NOTIF_KEY] ?: true }
    suspend fun setChatNotif(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[CHAT_NOTIF_KEY] = enabled }
    }


    // Matches the nt_day_streak and nt_last_active logic
    private val DAY_STREAK_KEY = intPreferencesKey("nt_day_streak")
    private val LAST_ACTIVE_KEY = stringPreferencesKey("nt_last_active")

    fun getDayStreak(): Flow<Int> = context.dataStore.data.map { it[DAY_STREAK_KEY] ?: 0 }
    
    suspend fun updateDayStreak(streak: Int, today: String) {
        context.dataStore.edit { prefs ->
            prefs[DAY_STREAK_KEY] = streak
            prefs[LAST_ACTIVE_KEY] = today
        }
    }
    
    fun getLastActive(): Flow<String> = context.dataStore.data.map { it[LAST_ACTIVE_KEY] ?: "" }

    fun getChatReadReceipt(chatId: String): Flow<Int> {
        val key = intPreferencesKey("nt_lr_$chatId")
        return context.dataStore.data.map { it[key] ?: 0 }
    }

    suspend fun updateChatReadReceipt(chatId: String, timestampSecs: Int) {
        val key = intPreferencesKey("nt_lr_$chatId")
        context.dataStore.edit { prefs ->
            prefs[key] = timestampSecs
        }
    }
}
