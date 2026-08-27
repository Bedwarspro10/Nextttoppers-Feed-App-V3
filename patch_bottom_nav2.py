import re

with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

pattern = r'if \(showBottomNav\) \{\s*androidx\.compose\.material3\.Surface\(.*?\n\s*\}\s*\}'
matches = re.findall(pattern, content, flags=re.DOTALL)

new_bottom_bar = """if (showBottomNav) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .androidx.compose.foundation.layout.windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.Companion.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .androidx.compose.foundation.layout.height(68.dp)
                        .androidx.compose.foundation.background(
                            color = androidx.compose.ui.graphics.Color(0xFF1E293B).copy(alpha = 0.85f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                        .androidx.compose.foundation.border(
                            width = 1.dp,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.9f else if (selected) 1.05f else 1f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
                        )
                        val activeBgAlpha by animateFloatAsState(targetValue = if (selected) 0.15f else 0f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = {
                                        if (!selected) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.layout.Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                                modifier = Modifier.scale(scale)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .androidx.compose.foundation.background(
                                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = activeBgAlpha),
                                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.size(24.dp)) {
                                        when (screen.route) {
                                            "home" -> com.example.core.designsystem.HomeIcon3D(modifier = Modifier.fillMaxSize())
                                            "courses" -> com.example.core.designsystem.CourseIcon3D(modifier = Modifier.fillMaxSize())
                                            "chat" -> com.example.core.designsystem.ChatIcon3D(modifier = Modifier.fillMaxSize())
                                            "leaderboard" -> com.example.core.designsystem.LeaderboardIcon3D(modifier = Modifier.fillMaxSize())
                                            else -> Icon(
                                                screen.icon, 
                                                contentDescription = screen.title,
                                                tint = if (selected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = screen.title,
                                    maxLines = 1,
                                    fontSize = androidx.compose.ui.unit.sp(11),
                                    fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                    color = if (selected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }"""

if matches:
    content = content.replace(matches[0], new_bottom_bar)
    with open("app/src/main/java/com/example/core/navigation/AppNavigation.kt", "w") as f:
        f.write(content)
    print("Patched bottom bar successfully!")
else:
    print("Could not find bottom bar to patch!")

