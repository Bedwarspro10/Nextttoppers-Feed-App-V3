with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

if "import androidx.compose.material.icons.filled.AccountBalanceWallet" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Star", "import androidx.compose.material.icons.filled.Star\nimport androidx.compose.material.icons.filled.AccountBalanceWallet")

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
