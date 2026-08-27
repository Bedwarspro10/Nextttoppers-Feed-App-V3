import re

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.foundation.layout.size", "import androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.fillMaxSize")

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
