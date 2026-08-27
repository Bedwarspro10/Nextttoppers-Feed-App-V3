import re

with open("app/src/main/java/com/example/feature/course/CourseScreen.kt", "r") as f:
    content = f.read()

pattern = r"Icon\(\s*imageVector = when \{\s*node.isFolder -> Icons.Default.Folder\s*isLecture -> Icons.Default.PlayCircle\s*else -> Icons.Default.PictureAsPdf\s*\},\s*contentDescription = null,\s*tint = if \(isPending\) Color.Gray else if \(node.isFolder\) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,\s*modifier = Modifier.size\(40.dp\)\s*\)"

replacement = """Box(modifier = Modifier.size(48.dp)) {
                when {
                    node.isFolder -> com.example.core.designsystem.FolderIcon3D(modifier = Modifier.fillMaxSize())
                    isLecture -> com.example.core.designsystem.VideoIcon3D(modifier = Modifier.fillMaxSize())
                    else -> com.example.core.designsystem.PdfIcon3D(modifier = Modifier.fillMaxSize())
                }
            }"""

content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/feature/course/CourseScreen.kt", "w") as f:
    f.write(content)
