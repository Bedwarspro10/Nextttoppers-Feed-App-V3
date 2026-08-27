import re

with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "r") as f:
    content = f.read()

old_announcement_icon = """                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = "Announcement",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 2.dp)
                    )"""

new_announcement_icon = """                    Box(modifier = Modifier.size(32.dp).padding(top = 2.dp)) {
                        com.example.core.designsystem.MegaphoneIcon3D(modifier = Modifier.fillMaxSize())
                    }"""

content = content.replace(old_announcement_icon, new_announcement_icon)

with open("app/src/main/java/com/example/feature/home/HomeScreen.kt", "w") as f:
    f.write(content)
