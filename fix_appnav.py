with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

old_call = """                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) }
                )"""

new_call = """                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                    onNavigateToWallet = { navController.navigate(Screen.Wallet.route) }
                )"""

content = content.replace(old_call, new_call)
with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
