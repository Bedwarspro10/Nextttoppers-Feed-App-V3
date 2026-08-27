package com.example.feature.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.di.AppContainer
import com.example.core.download.DownloadRepository
import com.example.data.local.DownloadEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DownloadsUiState(
    val downloads: List<DownloadEntity> = emptyList(),
    val isLoading: Boolean = true
)

class DownloadsViewModel(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    val uiState: StateFlow<DownloadsUiState> = downloadRepository.getAllDownloads()
        .map { downloads ->
            DownloadsUiState(
                downloads = downloads,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadsUiState(isLoading = true)
        )

    fun pauseDownload(id: String) {
        downloadRepository.pauseDownload(id)
    }
    
    fun resumeDownload(id: String) {
        downloadRepository.resumeDownload(id)
    }

    fun removeDownload(id: String) {
        downloadRepository.removeDownload(id)
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DownloadsViewModel(container.downloadRepository) as T
                }
            }
    }
}
