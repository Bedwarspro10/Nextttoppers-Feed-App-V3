import re

with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.ui.graphics.graphicsLayer" not in content:
    content = content.replace("import androidx.compose.ui.draw.scale", "import androidx.compose.ui.draw.scale\nimport androidx.compose.ui.graphics.graphicsLayer")

row_old = """        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(56.dp)) {"""

row_new = """        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconRotation by animateFloatAsState(
                targetValue = if (isPressed) -8f else 0f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
            )
            Box(modifier = Modifier.size(56.dp).graphicsLayer(rotationZ = iconRotation)) {"""

content = content.replace(row_old, row_new)

with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "w") as f:
    f.write(content)
