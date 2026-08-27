import re

with open("app/src/main/java/com/example/feature/leaderboard/LeaderboardScreen.kt", "r") as f:
    content = f.read()

# Add progress bar to the user header
old_header = """                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName ?: "You",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = LevelUtils.getLevelTitle(user.level),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = Color(0xFFF97316),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${user.streak} Day Streak",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }"""

new_header = """                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName ?: "You",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Lv.${user.level} - ${LevelUtils.getLevelTitle(user.level)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val nextLevelXp = LevelUtils.getXpForLevel(user.level + 1)
                                val currentLevelXp = LevelUtils.getXpForLevel(user.level)
                                val progress = if (nextLevelXp > currentLevelXp) {
                                    (user.xp - currentLevelXp).toFloat() / (nextLevelXp - currentLevelXp).toFloat()
                                } else 1f
                                
                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                )
                                Text(
                                    text = "${user.xp} / $nextLevelXp XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = Color(0xFFF97316),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${user.streak} Day Streak",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }"""

content = content.replace(old_header, new_header)

with open("app/src/main/java/com/example/feature/leaderboard/LeaderboardScreen.kt", "w") as f:
    f.write(content)
