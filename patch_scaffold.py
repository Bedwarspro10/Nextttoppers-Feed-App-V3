import re

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

pattern = r'Scaffold\(\s*containerColor = androidx\.compose\.ui\.graphics\.Color\.Transparent,\s*bottomBar = \{'
replacement = r'''Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {'''

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
        f.write(content)
    print("Patched Scaffold successfully!")
else:
    print("Could not find Scaffold pattern")

