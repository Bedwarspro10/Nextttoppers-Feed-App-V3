import re

with open("app/src/main/java/com/example/feature/home/HomeViewModel.kt", "r") as f:
    content = f.read()

new_init = """    init {
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
    }"""

content = content.replace("    init {\n        loadHomeData()\n    }", new_init)

with open("app/src/main/java/com/example/feature/home/HomeViewModel.kt", "w") as f:
    f.write(content)

