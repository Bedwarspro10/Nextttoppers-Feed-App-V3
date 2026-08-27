package com.example.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.datastore.LocalPreferences
import com.example.core.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val theme: String = "System Default",
    val reduceMotion: Boolean = false,
    val playbackQuality: String = "Auto",
    val playbackSpeed: String = "1.0x",
    val autoplayNext: Boolean = true,
    val downloadQuality: String = "High (1080p)",
    val downloadWifiOnly: Boolean = true,
    val announcementsNotif: Boolean = true,
    val chatNotif: Boolean = true,
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val localPreferences: LocalPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        localPreferences.getTheme(),
        localPreferences.getReduceMotion(),
        localPreferences.getPlaybackQuality(),
        localPreferences.getPlaybackSpeed(),
        localPreferences.getAutoplayNext(),
        localPreferences.getDownloadQuality(),
        localPreferences.getDownloadWifiOnly(),
        localPreferences.getAnnouncementsNotif(),
        localPreferences.getChatNotif()
    ) { params ->
        SettingsUiState(
            theme = params[0] as String,
            reduceMotion = params[1] as Boolean,
            playbackQuality = params[2] as String,
            playbackSpeed = params[3] as String,
            autoplayNext = params[4] as Boolean,
            downloadQuality = params[5] as String,
            downloadWifiOnly = params[6] as Boolean,
            announcementsNotif = params[7] as Boolean,
            chatNotif = params[8] as Boolean,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true)
    )

    fun setTheme(theme: String) {
        viewModelScope.launch {
            localPreferences.setTheme(theme)
        }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch {
            localPreferences.setReduceMotion(enabled)
        }
    }

    fun setPlaybackQuality(quality: String) {
        viewModelScope.launch {
            localPreferences.setPlaybackQuality(quality)
        }
    }

    fun setPlaybackSpeed(speed: String) {
        viewModelScope.launch {
            localPreferences.setPlaybackSpeed(speed)
        }
    }

    fun setAutoplayNext(enabled: Boolean) {
        viewModelScope.launch {
            localPreferences.setAutoplayNext(enabled)
        }
    }

    fun setDownloadQuality(quality: String) {
        viewModelScope.launch {
            localPreferences.setDownloadQuality(quality)
        }
    }

    fun setDownloadWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            localPreferences.setDownloadWifiOnly(enabled)
        }
    }

    fun setAnnouncementsNotif(enabled: Boolean) {
        viewModelScope.launch {
            localPreferences.setAnnouncementsNotif(enabled)
        }
    }

    fun setChatNotif(enabled: Boolean) {
        viewModelScope.launch {
            localPreferences.setChatNotif(enabled)
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(container.localPreferences) as T
                }
            }
    }
}
