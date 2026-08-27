import re

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Fix Profile route
old_profile = r"composable\(Screen\.Profile\.route\) \{[\s\S]*?com\.example\.feature\.profile\.ProfileScreen\([\s\S]*?onNavigateBack = \{ navController\.popBackStack\(\) \}[\s\S]*?\)\s*\}"

new_profile = """composable(Screen.Profile.route) {
                val viewModel: com.example.feature.home.HomeViewModel = viewModel(
                    factory = com.example.feature.home.HomeViewModel.provideFactory(appContainer)
                )
                com.example.feature.profile.ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSignOut = {
                        appContainer.authRepository.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }"""

content = re.sub(old_profile, new_profile, content)

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
