import re

with open("app/src/main/java/com/example/MainApplication.kt", "r") as f:
    content = f.read()

imports = """import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
"""

if "import android.app.NotificationChannel" not in content:
    content = content.replace("import android.app.Application", "import android.app.Application\n" + imports)

channel_setup = """        createNotificationChannel()
        
        container = DefaultAppContainer(this)"""

content = content.replace("container = DefaultAppContainer(this)", channel_setup)

channel_func = """
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "next_toppers_feed_channel",
                "General Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for chat and updates"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}"""

content = content.replace("}", channel_func)

# There is a risk of duplicate } or removing the class closing brace. Let's do it cleanly:
content = re.sub(r'\}\s*$', channel_func, content)

with open("app/src/main/java/com/example/MainApplication.kt", "w") as f:
    f.write(content)

