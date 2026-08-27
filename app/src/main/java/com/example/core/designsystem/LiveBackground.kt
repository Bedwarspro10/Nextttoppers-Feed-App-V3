package com.example.core.designsystem

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

import android.app.ActivityManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.firstOrNull

enum class DevicePerformanceClass {
    BUDGET,
    PRO
}

object PerformanceClassifier {
    fun getDevicePerformanceClass(context: Context): DevicePerformanceClass {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (activityManager != null) {
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
                if (totalRamGb < 5.0) {
                    return DevicePerformanceClass.BUDGET
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val mpc = android.os.Build.VERSION.MEDIA_PERFORMANCE_CLASS
                if (mpc < 31) {
                    return DevicePerformanceClass.BUDGET
                }
            }
            DevicePerformanceClass.PRO
        } catch (e: Exception) {
            DevicePerformanceClass.PRO
        }
    }
}

@Composable
fun LiveBackground() {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    
    // Auto detect performance tier
    val performanceClass = remember { PerformanceClassifier.getDevicePerformanceClass(context) }
    
    // Check reduce motion preference
    var reduceMotionActive by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            // Safely retrieve local preference value
            val prefs = com.example.core.datastore.LocalPreferences(context)
            prefs.getReduceMotion().firstOrNull()?.let {
                reduceMotionActive = it
            }
        } catch (e: Exception) {
            // Fallback
        }
    }

    val isBudgetOrReduced = performanceClass == DevicePerformanceClass.BUDGET || reduceMotionActive

    if (isBudgetOrReduced) {
        // Simplified/Opaque resource-saving background for Budget models or when reduceMotion is enabled
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            val bgColor1 = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
            val bgColor2 = if (isDark) Color(0xFF020617) else Color(0xFFF8FAFC)
            
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(bgColor1, bgColor2)
                )
            )
            
            // Render just 1 subtle static ambient blob to maintain visual depth without drawing overhead
            drawBlob(
                color = if (isDark) Color(0x183B82F6) else Color(0x103B82F6),
                centerX = w * 0.5f,
                centerY = h * 0.3f,
                radius = w * 0.6f
            )
        }
        return
    }

    // Flagship/Pro level: Animates 4 premium, floating glass blobs with infinite high-framerate curves (faster speed)
    val infiniteTransition = rememberInfiniteTransition(label = "LiveBackground")
    
    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle1"
    )
    
    val angle2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle2"
    )
    
    val angle3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle3"
    )
    
    val angle4 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle4"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Base background (Deep electric navy)
        val bgColor1 = if (isDark) Color(0xFF070B14) else Color(0xFFE0E7FF)
        val bgColor2 = if (isDark) Color(0xFF020408) else Color(0xFFF1F5F9)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(bgColor1, bgColor2),
                center = Offset(w / 2, h / 2),
                radius = w
            )
        )
        
        // Blobs
        val radius = w * 0.45f
        
        // Blob 1: Deep Indigo/Electric Violet
        drawBlob(
            color = if (isDark) Color(0x506366F1) else Color(0x356366F1),
            centerX = w * 0.25f + cos(angle1) * w * 0.22f,
            centerY = h * 0.25f + sin(angle1) * h * 0.15f,
            radius = radius
        )
        
        // Blob 2: Neon Cyan / Teal
        drawBlob(
            color = if (isDark) Color(0x4500E1BE) else Color(0x3000E1BE),
            centerX = w * 0.75f + cos(angle2) * w * 0.22f,
            centerY = h * 0.35f + sin(angle2) * h * 0.15f,
            radius = radius * 1.2f
        )
        
        // Blob 3: Neon Hot Pink / Magenta
        drawBlob(
            color = if (isDark) Color(0x38EC4899) else Color(0x25EC4899),
            centerX = w * 0.35f + cos(angle3) * w * 0.25f,
            centerY = h * 0.75f + sin(angle3) * h * 0.18f,
            radius = radius * 1.1f
        )
        
        // Blob 4: Bright Sunset Orange
        drawBlob(
            color = if (isDark) Color(0x28F59E0B) else Color(0x1CF59E0B),
            centerX = w * 0.7f + cos(angle4) * w * 0.2f,
            centerY = h * 0.65f + sin(angle4) * h * 0.14f,
            radius = radius * 0.9f
        )
    }
}

private fun DrawScope.drawBlob(color: Color, centerX: Float, centerY: Float, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = Offset(centerX, centerY),
            radius = radius
        ),
        radius = radius,
        center = Offset(centerX, centerY)
    )
}
