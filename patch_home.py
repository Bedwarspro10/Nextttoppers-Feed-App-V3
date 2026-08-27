import re

with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "r") as f:
    content = f.read()

# Update HomeScreen signature to pass onNavigateToWallet
old_sig = """fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit
) {"""

new_sig = """fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCourse: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToWallet: () -> Unit
) {"""

content = content.replace(old_sig, new_sig)

# Update HeaderSection call
old_call = """                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToDownloads = onNavigateToDownloads
                    )"""

new_call = """                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToDownloads = onNavigateToDownloads,
                        onNavigateToWallet = onNavigateToWallet
                    )"""

content = content.replace(old_call, new_call)

# Update HeaderSection signature
old_header_sig = """fun HeaderSection(
    name: String,
    photoUrl: String?,
    level: Int,
    streak: Int,
    coins: Int,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit
) {"""

new_header_sig = """fun HeaderSection(
    name: String,
    photoUrl: String?,
    level: Int,
    streak: Int,
    coins: Int,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToWallet: () -> Unit
) {"""

content = content.replace(old_header_sig, new_header_sig)

# Add clickable modifier to StatCard for Coins
old_coin_card = """        StatCard(
            modifier = Modifier.weight(1f),
            icon = { com.example.core.designsystem.CoinIcon3D(modifier = Modifier.fillMaxSize()) },
            label = "NT Coins",
            value = coins.toString()
        )"""

new_coin_card = """        StatCard(
            modifier = Modifier.weight(1f).clickable { onNavigateToWallet() },
            icon = { com.example.core.designsystem.CoinIcon3D(modifier = Modifier.fillMaxSize()) },
            label = "NT Coins",
            value = coins.toString()
        )"""

content = content.replace(old_coin_card, new_coin_card)

with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "w") as f:
    f.write(content)

