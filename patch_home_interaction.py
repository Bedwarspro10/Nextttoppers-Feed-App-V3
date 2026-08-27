import re

with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "r") as f:
    content = f.read()

# Make SubjectCard scale on press
imports = """import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
"""

if "import androidx.compose.animation.core.animateFloatAsState" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\n" + imports)


card_old = """@Composable
fun SubjectCard(
    subject: Subject,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp),
        shape = RoundedCornerShape(16.dp),"""

card_new = """@Composable
fun SubjectCard(
    subject: Subject,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 400f)
    )
    
    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),"""
        
content = content.replace(card_old, card_new)

with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "w") as f:
    f.write(content)
