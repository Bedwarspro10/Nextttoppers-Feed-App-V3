import re

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

old_home = """                com.example.feature.home.HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCourse = { courseId -> navController.navigate("course/$courseId") },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) }
                )"""

new_home = """                com.example.feature.home.HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCourse = { courseId -> navController.navigate("course/$courseId") },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                    onNavigateToWallet = { navController.navigate(Screen.Wallet.route) }
                )"""

content = content.replace(old_home, new_home)

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

