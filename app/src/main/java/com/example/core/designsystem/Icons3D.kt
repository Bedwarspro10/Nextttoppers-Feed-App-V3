package com.example.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MathIcon3D(modifier: Modifier = Modifier) {
    BookIcon3D(
        modifier = modifier,
        coverColors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9)),
        spineColors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)),
        symbol = "∑",
        symbolColor = Color(0xFF1E3A8A)
    )
}

@Composable
fun EnglishIcon3D(modifier: Modifier = Modifier) {
    BookIcon3D(
        modifier = modifier,
        coverColors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)),
        spineColors = listOf(Color(0xFF991B1B), Color(0xFF7F1D1D)),
        symbol = "A",
        symbolColor = Color(0xFF991B1B)
    )
}

@Composable
fun HindiIcon3D(modifier: Modifier = Modifier) {
    BookIcon3D(
        modifier = modifier,
        coverColors = listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA)),
        spineColors = listOf(Color(0xFFEA580C), Color(0xFFC2410C)),
        symbol = "अ",
        symbolColor = Color(0xFF9A3412)
    )
}

@Composable
fun BookIcon3D(
    modifier: Modifier = Modifier,
    coverColors: List<Color>,
    spineColors: List<Color>,
    symbol: String,
    symbolColor: Color
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Drop shadow
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .offset(y = 6.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .blur(6.dp)
        )
        
        // Pages (depth)
        Box(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .offset(x = 6.dp, y = 2.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))),
                    RoundedCornerShape(8.dp)
                )
        ) {
            Column(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp), verticalArrangement = Arrangement.SpaceEvenly) {
                repeat(4) {
                    Box(modifier = Modifier.fillMaxWidth(0.9f).height(1.dp).background(Color.White.copy(alpha = 0.5f)))
                }
            }
        }
        
        // Cover
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .offset(x = (-2).dp)
                .background(
                    Brush.linearGradient(
                        colors = coverColors,
                        start = Offset(0f, 0f), end = Offset.Infinite
                    ),
                    RoundedCornerShape(8.dp)
                )
        ) {
            // Spine
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.25f)
                    .background(
                        Brush.horizontalGradient(spineColors),
                        RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    )
            )
            // Accent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = symbolColor
                )
            }
        }
        
        // Front highlight
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .offset(x = (-2).dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                        start = Offset(0f, 0f), end = Offset(100f, 100f)
                    ),
                    RoundedCornerShape(8.dp)
                )
        )
    }
}

@Composable
fun ScienceIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(w*0.2f, h*0.8f),
                size = Size(w*0.6f, h*0.15f)
            )
            
            val flaskPath = Path().apply {
                moveTo(w*0.4f, h*0.1f)
                lineTo(w*0.6f, h*0.1f)
                lineTo(w*0.6f, h*0.4f)
                lineTo(w*0.9f, h*0.85f)
                quadraticBezierTo(w*0.95f, h*0.95f, w*0.8f, h*0.95f)
                lineTo(w*0.2f, h*0.95f)
                quadraticBezierTo(w*0.05f, h*0.95f, w*0.1f, h*0.85f)
                lineTo(w*0.4f, h*0.4f)
                close()
            }
            
            // Liquid
            val liquidPath = Path().apply {
                moveTo(w*0.25f, h*0.6f)
                lineTo(w*0.75f, h*0.6f)
                lineTo(w*0.88f, h*0.83f)
                quadraticBezierTo(w*0.92f, h*0.92f, w*0.8f, h*0.92f)
                lineTo(w*0.2f, h*0.92f)
                quadraticBezierTo(w*0.08f, h*0.92f, w*0.12f, h*0.83f)
                close()
            }
            
            drawPath(
                path = liquidPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF34D399), Color(0xFF047857)),
                    start = Offset(0f, h*0.6f),
                    end = Offset(w, h)
                )
            )
            
            // Glass reflection/border
            drawPath(
                path = flaskPath,
                color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )
            
            // Glass highlight
            val highlightPath = Path().apply {
                moveTo(w*0.42f, h*0.12f)
                lineTo(w*0.42f, h*0.38f)
                lineTo(w*0.18f, h*0.8f)
            }
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.8f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
    }
}

@Composable
fun GlobeIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w/2, h/2)
            val radius = w * 0.4f
            
            drawOval(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(w*0.2f, h*0.85f),
                size = Size(w*0.6f, h*0.15f)
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF60A5FA), Color(0xFF2563EB), Color(0xFF1E3A8A)),
                    center = Offset(w*0.3f, h*0.3f),
                    radius = radius * 1.2f
                ),
                radius = radius,
                center = center
            )
            
            val landPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(w*0.1f, h*0.3f, w*0.5f, h*0.6f))
                addOval(androidx.compose.ui.geometry.Rect(w*0.5f, h*0.2f, w*0.9f, h*0.6f))
                addOval(androidx.compose.ui.geometry.Rect(w*0.3f, h*0.6f, w*0.7f, h*0.9f))
            }
            clipPath(Path().apply { addOval(androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)) }) {
                drawPath(
                    path = landPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF34D399), Color(0xFF064E3B)),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )
            }
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                    center = Offset(w*0.5f, h*0.5f),
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(w*0.3f, h*0.3f),
                    radius = radius * 0.5f
                ),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
fun NotesIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Shadow
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .offset(y = 6.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .blur(6.dp)
        )
        // Folder Back
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .offset(y = (-4).dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                    RoundedCornerShape(8.dp)
                )
        )
        // Paper
        Box(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .offset(x = 4.dp, y = (-8).dp)
                .rotate(5f)
                .background(
                    Brush.linearGradient(listOf(Color.White, Color(0xFFF1F5F9))),
                    RoundedCornerShape(4.dp)
                )
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(4.dp).background(Color(0xFFCBD5E1)))
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(4.dp).background(Color(0xFFCBD5E1)))
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(4.dp).background(Color(0xFFCBD5E1)))
            }
        }
        // Folder Front
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF60A5FA), Color(0xFF2563EB)),
                        start = Offset(0f, 0f), end = Offset.Infinite
                    ),
                    RoundedCornerShape(8.dp)
                )
        )
        // Highlight
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                        start = Offset(0f, 0f), end = Offset(100f, 100f)
                    ),
                    RoundedCornerShape(8.dp)
                )
        )
    }
}

@Composable
fun FlameIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            drawOval(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(w*0.2f, h*0.85f),
                size = Size(w*0.6f, h*0.15f)
            )
            
            val flamePath = Path().apply {
                moveTo(w*0.5f, h*0.1f)
                quadraticBezierTo(w*0.85f, h*0.5f, w*0.8f, h*0.75f)
                cubicTo(w*0.8f, h*0.95f, w*0.5f, h*0.95f, w*0.5f, h*0.95f)
                cubicTo(w*0.5f, h*0.95f, w*0.2f, h*0.95f, w*0.2f, h*0.75f)
                quadraticBezierTo(w*0.15f, h*0.5f, w*0.5f, h*0.1f)
            }
            drawPath(
                path = flamePath,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDE047), Color(0xFFF97316), Color(0xFF991B1B)),
                    center = Offset(w*0.5f, h*0.75f),
                    radius = w*0.5f
                )
            )
            
            val innerFlame = Path().apply {
                moveTo(w*0.5f, h*0.4f)
                quadraticBezierTo(w*0.65f, h*0.65f, w*0.65f, h*0.8f)
                cubicTo(w*0.65f, h*0.9f, w*0.5f, h*0.9f, w*0.5f, h*0.9f)
                cubicTo(w*0.5f, h*0.9f, w*0.35f, h*0.9f, w*0.35f, h*0.8f)
                quadraticBezierTo(w*0.35f, h*0.65f, w*0.5f, h*0.4f)
            }
            drawPath(
                path = innerFlame,
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFFEF08A), Color(0xFFEA580C)),
                    center = Offset(w*0.5f, h*0.8f),
                    radius = w*0.3f
                )
            )
        }
    }
}

@Composable
fun CoinIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w/2, h/2)
            val radius = w * 0.4f
            
            drawOval(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(w*0.2f, h*0.85f),
                size = Size(w*0.6f, h*0.15f)
            )
            
            // Edge
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFB45309), Color(0xFF78350F)),
                    start = Offset(0f, 0f), end = Offset(w, h)
                ),
                radius = radius,
                center = Offset(center.x, center.y + h*0.06f)
            )
            
            // Front
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFEF08A), Color(0xFFEAB308), Color(0xFFA16207)),
                    start = Offset(w*0.2f, h*0.2f), end = Offset(w*0.8f, h*0.8f)
                ),
                radius = radius,
                center = center
            )
            
            // Inner engrave
            drawCircle(
                color = Color(0xFF78350F).copy(alpha = 0.4f),
                radius = radius * 0.75f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.7f), Color.Transparent),
                    center = Offset(w*0.3f, h*0.3f),
                    radius = radius * 0.6f
                ),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
fun MegaphoneIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(w*0.1f, h*0.75f),
                size = Size(w*0.8f, h*0.2f)
            )
            
            // Handle
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF64748B), Color(0xFF334155))
                ),
                topLeft = Offset(w*0.35f, h*0.5f),
                size = Size(w*0.15f, h*0.35f),
                cornerRadius = CornerRadius(w*0.05f)
            )
            
            // Body (Cone)
            val conePath = Path().apply {
                moveTo(w*0.2f, h*0.4f)
                lineTo(w*0.8f, h*0.1f)
                lineTo(w*0.8f, h*0.7f)
                lineTo(w*0.2f, h*0.4f)
                close()
            }
            drawPath(
                path = conePath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)),
                    start = Offset(0f, 0f), end = Offset(w, h)
                )
            )
            
            // Front opening
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF334155), Color(0xFF0F172A))
                ),
                topLeft = Offset(w*0.75f, h*0.1f),
                size = Size(w*0.1f, h*0.6f)
            )
            
            // Back end cap
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFEF4444), Color(0xFF991B1B))
                ),
                topLeft = Offset(w*0.15f, h*0.3f),
                size = Size(w*0.1f, h*0.2f)
            )
            
            // Lines representing sound
            drawOval(
                color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                topLeft = Offset(w*0.9f, h*0.25f),
                size = Size(w*0.05f, h*0.3f)
            )
            drawOval(
                color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                topLeft = Offset(w*0.95f, h*0.2f),
                size = Size(w*0.05f, h*0.4f)
            )
        }
    }
}

@Composable
fun FolderIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Shadow
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .offset(y = 4.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .blur(4.dp)
        )
        // Back folder tab
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
                .offset(y = (-4).dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7))),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                )
        )
        // Front folder cover
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF7DD3FC), Color(0xFF0EA5E9)),
                        start = Offset(0f, 0f), end = Offset.Infinite
                    ),
                    RoundedCornerShape(4.dp)
                )
        )
        // Highlight
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                        start = Offset(0f, 0f), end = Offset(50f, 50f)
                    ),
                    RoundedCornerShape(4.dp)
                )
        )
    }
}

@Composable
fun PdfIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Shadow
        Box(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .offset(x = 2.dp, y = 4.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .blur(4.dp)
        )
        // Document base
        Box(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1))),
                    RoundedCornerShape(4.dp)
                )
        )
        // Red PDF accent
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(0.4f)
                .align(Alignment.Center)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFF87171), Color(0xFFDC2626))),
                    RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("PDF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

@Composable
fun VideoIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Shadow
        Box(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .offset(y = 4.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .blur(4.dp)
        )
        // Video board
        Box(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF9CA3AF), Color(0xFF4B5563))),
                    RoundedCornerShape(8.dp)
                )
        )
        // Clapper strip
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.2f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF1F2937), Color(0xFF111827))),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
        )
        // Play button
        Canvas(modifier = Modifier.size(24.dp).align(Alignment.Center)) {
            val path = Path().apply {
                moveTo(size.width * 0.3f, size.height * 0.2f)
                lineTo(size.width * 0.8f, size.height * 0.5f)
                lineTo(size.width * 0.3f, size.height * 0.8f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF3B82F6)))
            )
        }
    }
}

@Composable
fun HomeIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Roof
            val roofPath = Path().apply {
                moveTo(w*0.5f, h*0.1f)
                lineTo(w*0.9f, h*0.5f)
                lineTo(w*0.1f, h*0.5f)
                close()
            }
            drawPath(
                path = roofPath,
                brush = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706)))
            )
            // Base
            drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB))),
                topLeft = Offset(w*0.2f, h*0.5f),
                size = Size(w*0.6f, h*0.4f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            // Door
            drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF172554))),
                topLeft = Offset(w*0.4f, h*0.6f),
                size = Size(w*0.2f, h*0.3f)
            )
        }
    }
}

@Composable
fun CourseIcon3D(modifier: Modifier = Modifier) {
    MathIcon3D(modifier)
}

@Composable
fun ChatIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            drawOval(
                brush = Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669))),
                topLeft = Offset(w*0.1f, h*0.1f),
                size = Size(w*0.8f, h*0.6f)
            )
            val tailPath = Path().apply {
                moveTo(w*0.2f, h*0.6f)
                lineTo(w*0.1f, h*0.9f)
                lineTo(w*0.4f, h*0.65f)
                close()
            }
            drawPath(
                path = tailPath,
                brush = Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))
            )
            
            // Dots
            drawCircle(Color.White, radius = w*0.06f, center = Offset(w*0.3f, h*0.4f))
            drawCircle(Color.White, radius = w*0.06f, center = Offset(w*0.5f, h*0.4f))
            drawCircle(Color.White, radius = w*0.06f, center = Offset(w*0.7f, h*0.4f))
        }
    }
}

@Composable
fun LeaderboardIcon3D(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Shadow
            drawOval(Color.Black.copy(alpha = 0.3f), topLeft = Offset(w*0.1f, h*0.8f), size = Size(w*0.8f, h*0.2f))
            // Star
            val path = Path().apply {
                moveTo(w*0.5f, h*0.1f)
                lineTo(w*0.65f, h*0.4f)
                lineTo(w*0.95f, h*0.45f)
                lineTo(w*0.75f, h*0.65f)
                lineTo(w*0.8f, h*0.95f)
                lineTo(w*0.5f, h*0.8f)
                lineTo(w*0.2f, h*0.95f)
                lineTo(w*0.25f, h*0.65f)
                lineTo(w*0.05f, h*0.45f)
                lineTo(w*0.35f, h*0.4f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDE047), Color(0xFFEAB308), Color(0xFFB45309)),
                    center = Offset(w*0.5f, h*0.5f),
                    radius = w*0.5f
                )
            )
        }
    }
}
