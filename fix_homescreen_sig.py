import re
with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "r") as f:
    content = f.read()

old_sig = """fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSubject: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit
)"""

new_sig = """fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSubject: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToWallet: () -> Unit
)"""

content = content.replace(old_sig, new_sig)

with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "w") as f:
    f.write(content)
