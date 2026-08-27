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
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val localPreferences: LocalPreferences
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        localPreferences.getTheme(),
        localPreferences.getReduceMotion()
    ) { theme, reduceMotion ->
        SettingsUiState(
            theme = theme,
            reduceMotion = reduceMotion,
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
