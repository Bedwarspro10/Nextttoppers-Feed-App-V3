import re

with open("app/src/main/java/com/example/feature/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
"""

content = content.replace("import androidx.compose.ui.Alignment", imports + "import androidx.compose.ui.Alignment")

about_section = """                item {
                    SettingsSection("ABOUT") {"""

app_section = """                item {
                    SettingsSection("APP") {
                        val context = LocalContext.current
                        val coroutineScope = rememberCoroutineScope()
                        SettingsActionItem("Export APK", "Save this app's APK to Downloads folder", onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                exportApk(context)
                            }
                        })
                    }
                }
                item {
                    SettingsSection("ABOUT") {"""

content = content.replace(about_section, app_section)

export_function = """
private suspend fun exportApk(context: Context) {
    withContext(Dispatchers.IO) {
        try {
            val applicationInfo = context.applicationInfo
            val apkFile = File(applicationInfo.sourceDir)
            val fileName = "NextToppersFeed_Export.apk"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/vnd.android.package-archive")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        FileInputStream(apkFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "APK exported to Downloads", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, fileName)
                FileInputStream(apkFile).use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "APK exported to Downloads", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to export APK", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
"""

content = content + export_function

with open("app/src/main/java/com/example/feature/settings/SettingsScreen.kt", "w") as f:
    f.write(content)

