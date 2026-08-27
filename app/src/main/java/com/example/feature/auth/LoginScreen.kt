package com.example.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    // Handle navigation immediately if authenticated
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onNavigateToHome()
        }
    }

    // Animation state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent) // LiveBackground handles the animated colors
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Spacer(modifier = Modifier.weight(0.8f))

            // TOP / CENTER AREA (Branding)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 80f)
                    )
                ) {
                    Text(
                        text = "Aarambh",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        style = MaterialTheme.typography.displayLarge.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White, Color(0xFFE2E8F0))
                            )
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 200)) + slideInVertically(
                        initialOffsetY = { 20 },
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 80f)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NEXT TOPPERS FEED",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Your learning journey starts here.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ERROR MESSAGE
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color(0xFFFF5252),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // BUTTON AREA
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 450)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f)
                )
            ) {
                val isLoading = authState is AuthState.Loading
                
                // Custom Google Button with press animation
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.95f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                    label = "button_scale"
                )
                
                val alpha by animateFloatAsState(
                    targetValue = if (isPressed) 0.8f else 1f,
                    animationSpec = tween(150),
                    label = "button_alpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp)
                        .scale(scale)
                        .graphicsLayer(alpha = alpha)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = !isLoading,
                            onClick = { viewModel.signInWithGoogle() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = googleIcon(),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(22.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // BOTTOM TEXT
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 600))
            ) {
                Text(
                    text = "By continuing, you agree to use Next Toppers Feed responsibly.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Google Icon Vector
private fun googleIcon(): ImageVector {
    return ImageVector.Builder(
        name = "Google",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF4285F4))) {
            moveTo(22.56f, 12.23f)
            curveTo(22.56f, 11.39f, 22.48f, 10.58f, 22.34f, 9.8f)
            lineTo(12f, 9.8f)
            lineTo(12f, 14.39f)
            lineTo(17.92f, 14.39f)
            curveTo(17.66f, 15.88f, 16.82f, 17.15f, 15.54f, 18.0f)
            lineTo(15.54f, 21.57f)
            lineTo(19.11f, 21.57f)
            curveTo(21.19f, 19.64f, 22.56f, 16.2f, 22.56f, 12.23f)
            close()
        }
        path(fill = SolidColor(Color(0xFF34A853))) {
            moveTo(12f, 23f)
            curveTo(14.97f, 23f, 17.47f, 22.01f, 19.11f, 20.17f)
            lineTo(15.54f, 16.6f)
            curveTo(14.65f, 17.2f, 13.43f, 17.58f, 12f, 17.58f)
            curveTo(9.24f, 17.58f, 6.89f, 15.71f, 6.06f, 13.2f)
            lineTo(2.38f, 13.2f)
            lineTo(2.38f, 16.82f)
            curveTo(4.15f, 20.35f, 7.78f, 23f, 12f, 23f)
            close()
        }
        path(fill = SolidColor(Color(0xFFFBBC05))) {
            moveTo(6.06f, 13.2f)
            curveTo(5.84f, 12.55f, 5.72f, 11.86f, 5.72f, 11.16f)
            curveTo(5.72f, 10.45f, 5.84f, 9.77f, 6.06f, 9.11f)
            lineTo(6.06f, 5.5f)
            lineTo(2.38f, 5.5f)
            curveTo(1.65f, 6.96f, 1.23f, 8.56f, 1.23f, 10.27f)
            curveTo(1.23f, 11.97f, 1.65f, 13.57f, 2.38f, 15.03f)
            lineTo(6.06f, 13.2f)
            close()
        }
        path(fill = SolidColor(Color(0xFFEA4335))) {
            moveTo(12f, 4.75f)
            curveTo(13.62f, 4.75f, 15.07f, 5.31f, 16.21f, 6.4f)
            lineTo(19.2f, 3.41f)
            curveTo(17.47f, 1.79f, 14.96f, 0.77f, 12f, 0.77f)
            curveTo(7.78f, 0.77f, 4.15f, 3.42f, 2.38f, 6.95f)
            lineTo(6.06f, 10.57f)
            curveTo(6.89f, 8.06f, 9.24f, 4.75f, 12f, 4.75f)
            close()
        }
    }.build()
}
