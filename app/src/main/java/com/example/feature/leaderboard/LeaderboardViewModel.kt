package com.example.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.di.AppContainer
import com.example.data.models.AppUser
import com.example.data.repositories.LeaderboardRepository
import com.example.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val period: String = "week", // week, month, all_time
    val users: List<AppUser> = emptyList(),
    val currentUser: AppUser? = null,
    val error: String? = null
)

class LeaderboardViewModel(
    private val leaderboardRepository: LeaderboardRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
        loadLeaderboard("week")
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getUserFlow().collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }
    }

    fun setPeriod(period: String) {
        if (_uiState.value.period == period) return
        _uiState.value = _uiState.value.copy(period = period)
        loadLeaderboard(period)
    }

    private fun loadLeaderboard(period: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                leaderboardRepository.getLeaderboard(period).collect { users ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        users = users
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load leaderboard"
                )
            }
        }
    }

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val leaderboardRepo = LeaderboardRepository(container.firestore)
                    val userRepo = UserRepository(container.firebaseAuth, container.firestore)
                    return LeaderboardViewModel(leaderboardRepo, userRepo) as T
                }
            }
    }
}
