import re

with open("app/src/main/java/com/example/feature/course/CourseScreen.kt", "r") as f:
    content = f.read()

# Make icon clickable scaling? Actually the item is clickable anyway.
# We just replace the Icon(...) block

old_icon_block = """                        Icon(
                imageVector = when {
                    node.isFolder -> Icons.Default.Folder
                    isLecture -> Icons.Default.PlayCircle
                    else -> Icons.Default.PictureAsPdf
                },
                contentDescription = null,
                tint = if (isPending) Color.Gray else if (node.isFolder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )"""

new_icon_block = """                        Box(modifier = Modifier.size(48.dp)) {
                when {
                    node.isFolder -> com.example.core.designsystem.FolderIcon3D(modifier = Modifier.fillMaxSize())
                    isLecture -> com.example.core.designsystem.VideoIcon3D(modifier = Modifier.fillMaxSize())
                    else -> com.example.core.designsystem.PdfIcon3D(modifier = Modifier.fillMaxSize())
                }
            }"""

content = content.replace(old_icon_block, new_icon_block)

with open("app/src/main/java/com/example/feature/course/CourseScreen.kt", "w") as f:
    f.write(content)

