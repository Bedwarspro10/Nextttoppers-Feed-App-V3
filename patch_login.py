import re

with open("app/src/main/java/com/example/feature/auth/LoginScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.graphics.SolidColor", "import androidx.compose.ui.graphics.SolidColor\nimport androidx.compose.ui.graphics.Brush")

old_box = """    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .windowInsetsPadding(WindowInsets.systemBars), // respect system bars
        contentAlignment = Alignment.Center
    ) {"""

new_box = """    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF020617).copy(alpha = 0.6f),
                        Color(0xFF0F172A).copy(alpha = 0.95f)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {"""

content = content.replace(old_box, new_box)

with open("app/src/main/java/com/example/feature/auth/LoginScreen.kt", "w") as f:
    f.write(content)

