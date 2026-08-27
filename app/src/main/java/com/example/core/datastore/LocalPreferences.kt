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
    
    fun getTheme(): Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "System Default" }
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs -> prefs[THEME_KEY] = theme }
    }
    
    fun getReduceMotion(): Flow<Boolean> = context.dataStore.data.map { it[REDUCE_MOTION_KEY] ?: false }
    suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[REDUCE_MOTION_KEY] = enabled }
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
