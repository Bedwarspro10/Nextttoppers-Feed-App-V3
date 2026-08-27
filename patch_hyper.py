with open("app/src/main/java/com/example/core/designsystem/HyperOsMotion.kt", "r") as f:
    content = f.read()

import re

# Add slideInVertically and slideOutVertically imports
if "import androidx.compose.animation.slideInVertically" not in content:
    content = content.replace("import androidx.compose.animation.scaleOut", "import androidx.compose.animation.scaleOut\nimport androidx.compose.animation.slideInVertically\nimport androidx.compose.animation.slideOutVertically\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.platform.LocalDensity")

new_enter = """val enterTransition: EnterTransition = 
        scaleIn(
            initialScale = 0.85f,
            animationSpec = openSpringSpec,
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300, easing = hyperEasing)
        ) + slideInVertically(
            initialOffsetY = { 150 }, // ~50dp approx
            animationSpec = openSpringSpec
        )"""

new_pop_exit = """val popExitTransition: ExitTransition = 
        scaleOut(
            targetScale = 0.85f,
            animationSpec = closeSpringSpec,
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 250, easing = hyperEasing)
        ) + slideOutVertically(
            targetOffsetY = { 150 },
            animationSpec = closeSpringSpec
        )"""

content = re.sub(r'val enterTransition.*?\)\s*\)\s*', new_enter + "\n\n    ", content, flags=re.DOTALL)
content = re.sub(r'val popExitTransition.*?\)\s*\)\s*', new_pop_exit + "\n\n    ", content, flags=re.DOTALL)

with open("app/src/main/java/com/example/core/designsystem/HyperOsMotion.kt", "w") as f:
    f.write(content)
