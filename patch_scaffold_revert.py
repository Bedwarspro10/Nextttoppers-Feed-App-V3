import re

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace("contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),", "")

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)

