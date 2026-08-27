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

@Composable
fun LiveBackground() {
    val isDark = isSystemInDarkTheme()
    
    // We will animate the offset angles for 4 blobs
    val infiniteTransition = rememberInfiniteTransition(label = "LiveBackground")
    
    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle1"
    )
    
    val angle2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle2"
    )
    
    val angle3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(17000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle3"
    )
    
    val angle4 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(19000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle4"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Base background
        val bgColor1 = if (isDark) Color(0xFF111322) else Color(0xFFE2E8F0)
        val bgColor2 = if (isDark) Color(0xFF040408) else Color(0xFFF8FAFC)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(bgColor1, bgColor2),
                center = Offset(w / 2, h / 2),
                radius = w
            )
        )
        
        // Blobs
        val radius = w * 0.4f
        
        // Blob 1 (Blue)
        drawBlob(
            color = if (isDark) Color(0x402878FF) else Color(0x302878FF),
            centerX = w * 0.2f + cos(angle1) * w * 0.2f,
            centerY = h * 0.2f + sin(angle1) * h * 0.1f,
            radius = radius
        )
        
        // Blob 2 (Cyan)
        drawBlob(
            color = if (isDark) Color(0x3000E1BE) else Color(0x2000E1BE),
            centerX = w * 0.8f + cos(angle2) * w * 0.2f,
            centerY = h * 0.3f + sin(angle2) * h * 0.1f,
            radius = radius * 1.2f
        )
        
        // Blob 3 (Purple)
        drawBlob(
            color = if (isDark) Color(0x289B41FF) else Color(0x189B41FF),
            centerX = w * 0.3f + cos(angle3) * w * 0.3f,
            centerY = h * 0.8f + sin(angle3) * h * 0.2f,
            radius = radius * 1.1f
        )
        
        // Blob 4 (Pink)
        drawBlob(
            color = if (isDark) Color(0x18FF4691) else Color(0x10FF4691),
            centerX = w * 0.7f + cos(angle4) * w * 0.2f,
            centerY = h * 0.7f + sin(angle4) * h * 0.15f,
            radius = radius
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
