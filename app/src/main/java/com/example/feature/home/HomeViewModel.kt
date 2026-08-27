package com.example.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.di.AppContainer
import com.example.data.models.Announcement
import com.example.data.models.AppUser
import com.example.data.models.Banner
import com.example.data.models.CoinWalletData
import com.example.data.models.Subject
import com.example.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: AppUser? = null,
    val wallet: CoinWalletData? = null,
    val banners: List<Banner> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userRepository.getUserFlow().collect { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(user = user)
                }
            }
        }
        viewModelScope.launch {
            userRepository.getWalletFlow().collect { wallet ->
                _uiState.value = _uiState.value.copy(wallet = wallet)
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch all data concurrently
                val user = userRepository.getOrCreateUser()
                val wallet = userRepository.getWalletData()
                val banners = homeRepository.getActiveBanners()
                val announcements = homeRepository.getLatestAnnouncements()
                val subjects = homeRepository.getSubjects()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    wallet = wallet,
                    banners = banners,
                    announcements = announcements,
                    subjects = subjects
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load home data"
                )
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val userRepo = UserRepository(container.firebaseAuth, container.firestore)
                    val homeRepo = HomeRepository(container.firestore)
                    return HomeViewModel(userRepo, homeRepo) as T
                }
            }
    }
}
