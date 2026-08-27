import re

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

# Remove YouTube from BottomNavItems
bottom_nav_old = """val BottomNavItems = listOf(
    Screen.Home,
    Screen.Courses,
    Screen.YouTube,
    Screen.Chat,
    Screen.Leaderboard
)"""
bottom_nav_new = """val BottomNavItems = listOf(
    Screen.Home,
    Screen.Courses,
    Screen.Chat,
    Screen.Leaderboard
)"""
content = content.replace(bottom_nav_old, bottom_nav_new)

# Update BottomNavigation icons and add press animations
# I will define a custom composable right before Scaffold, or inside.
imports = """import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
"""

if "import androidx.compose.animation.core.spring" not in content:
    content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\n" + imports)

# We need to replace NavigationBarItem
nav_item_old = """                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.ui.graphics.Color.White,
                                    selectedTextColor = androidx.compose.ui.graphics.Color.White,
                                    indicatorColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                                    unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)
                                ),
                                onClick = {"""

nav_item_new = """                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.85f else if (selected) 1.1f else 1f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
                            )
                            
                            NavigationBarItem(
                                icon = { 
                                    Box(modifier = Modifier.size(28.dp).scale(scale)) {
                                        when (screen.route) {
                                            "home" -> com.example.core.designsystem.HomeIcon3D(modifier = Modifier.fillMaxSize())
                                            "courses" -> com.example.core.designsystem.CourseIcon3D(modifier = Modifier.fillMaxSize())
                                            "chat" -> com.example.core.designsystem.ChatIcon3D(modifier = Modifier.fillMaxSize())
                                            "leaderboard" -> com.example.core.designsystem.LeaderboardIcon3D(modifier = Modifier.fillMaxSize())
                                            else -> Icon(screen.icon, contentDescription = screen.title)
                                        }
                                    }
                                },
                                label = { Text(screen.title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                                selected = selected,
                                interactionSource = interactionSource,
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = androidx.compose.ui.graphics.Color.White,
                                    selectedTextColor = androidx.compose.ui.graphics.Color.White,
                                    indicatorColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                                    unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)
                                ),
                                onClick = {"""

content = content.replace(nav_item_old, nav_item_new)

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
