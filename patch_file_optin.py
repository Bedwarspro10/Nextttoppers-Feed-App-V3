with open("app/src/main/java/com/example/feature/course/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

optin = "@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.media3.common.util.UnstableApi::class)\n"

content = optin + content

with open("app/src/main/java/com/example/feature/course/VideoPlayerScreen.kt", "w") as f:
    f.write(content)
