with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace(".androidx.compose.foundation.layout.windowInsetsPadding", ".windowInsetsPadding")
content = content.replace("androidx.compose.foundation.layout.WindowInsets.Companion.navigationBars", "WindowInsets.navigationBars")
content = content.replace(".androidx.compose.foundation.layout.height", ".height")
content = content.replace(".androidx.compose.foundation.background", ".background")
content = content.replace(".androidx.compose.foundation.border", ".border")
content = content.replace("androidx.compose.foundation.layout.Row", "Row")
content = content.replace("androidx.compose.foundation.layout.Arrangement.SpaceEvenly", "Arrangement.SpaceEvenly")
content = content.replace("androidx.compose.foundation.layout.Column", "Column")
content = content.replace("androidx.compose.foundation.layout.Arrangement.Center", "Arrangement.Center")
content = content.replace("androidx.compose.foundation.layout.Spacer", "Spacer")
content = content.replace("androidx.compose.ui.unit.sp", "sp")
content = content.replace("androidx.compose.ui.text.font.FontWeight.Bold", "FontWeight.Bold")
content = content.replace("androidx.compose.ui.text.font.FontWeight.Normal", "FontWeight.Normal")
content = content.replace("androidx.compose.ui.graphics.Color", "Color")
content = content.replace("androidx.compose.foundation.shape.RoundedCornerShape", "RoundedCornerShape")

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
