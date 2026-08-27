import re

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

imports = """import com.example.feature.wallet.WalletScreen
import com.example.feature.leaderboard.LeaderboardScreen
import com.example.feature.leaderboard.LeaderboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
"""
if "import com.example.feature.wallet.WalletScreen" not in content:
    content = content.replace("import androidx.compose.runtime.Composable", imports + "\nimport androidx.compose.runtime.Composable")

# Ensure Wallet route exists
if "object Wallet" not in content:
    content = content.replace("object Settings", "object Wallet : Screen(\"wallet\", \"Wallet\", Icons.Filled.AccountBalanceWallet)\n    object Settings")


wallet_route = """            composable(Screen.Wallet.route) {
                val viewModel: com.example.feature.home.HomeViewModel = viewModel(
                    factory = com.example.feature.home.HomeViewModel.provideFactory(appContainer)
                )
                WalletScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
"""

if "composable(Screen.Wallet.route)" not in content:
    content = content.replace("composable(Screen.Profile.route) {", wallet_route + "            composable(Screen.Profile.route) {")

# LeaderboardScreen usage
leaderboard_old = """            composable(Screen.Leaderboard.route) {
                // LeaderboardScreen(appContainer)
            }"""

leaderboard_new = """            composable(Screen.Leaderboard.route) {
                val viewModel: com.example.feature.leaderboard.LeaderboardViewModel = viewModel(
                    factory = com.example.feature.leaderboard.LeaderboardViewModel.provideFactory(appContainer)
                )
                LeaderboardScreen(viewModel = viewModel)
            }"""

content = content.replace(leaderboard_old, leaderboard_new)

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

