package com.example.core.designsystem

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.TransformOrigin

object HyperOsMotion {
    
    // HyperOS Physics Spec: High responsiveness with clean mechanical damping
    val openSpringSpec = spring<Float>(
        dampingRatio = 0.74f,      // Fluid, natural rebound
        stiffness = Spring.StiffnessMediumLow // Quick snap velocity
    )
    
    val closeSpringSpec = spring<Float>(
        dampingRatio = 0.88f,      // Tighter control on collapse to avoid shaking
        stiffness = Spring.StiffnessMedium    // Snappy closure velocity
    )
    
    val hyperEasing = CubicBezierEasing(0.15f, 1.0f, 0.3f, 1.0f)
    
    val fastTween = tween<Float>(durationMillis = 250, easing = hyperEasing)

    // Forward entrance (Home -> Detail)
    val enterTransition: EnterTransition = 
        scaleIn(
            initialScale = 0.85f,
            animationSpec = spring(dampingRatio = 0.74f, stiffness = Spring.StiffnessMediumLow),
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300, easing = hyperEasing)
        ) + slideInVertically(
            initialOffsetY = { 150 }, // ~50dp approx
            animationSpec = spring(dampingRatio = 0.74f, stiffness = Spring.StiffnessMediumLow)
        )
    
    // Forward exit (Home leaves as Detail enters)
    val exitTransition: ExitTransition = 
        scaleOut(
            targetScale = 1.05f,
            animationSpec = spring(dampingRatio = 0.88f, stiffness = Spring.StiffnessMedium),
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 250, easing = hyperEasing)
        )
    
    // Backward entrance (Detail -> Home, Home enters)
    val popEnterTransition: EnterTransition = 
        scaleIn(
            initialScale = 1.05f,
            animationSpec = spring(dampingRatio = 0.74f, stiffness = Spring.StiffnessMediumLow),
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300, easing = hyperEasing)
        )
    
    // Backward exit (Detail leaves as Home enters)
    val popExitTransition: ExitTransition = 
        scaleOut(
            targetScale = 0.85f,
            animationSpec = spring(dampingRatio = 0.88f, stiffness = Spring.StiffnessMedium),
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) + fadeOut(
            animationSpec = tween(durationMillis = 250, easing = hyperEasing)
        ) + slideOutVertically(
            targetOffsetY = { 150 },
            animationSpec = spring(dampingRatio = 0.88f, stiffness = Spring.StiffnessMedium)
        )

    // Subtle transitions for bottom navigation tabs
    val tabEnterTransition: EnterTransition = 
        scaleIn(
            initialScale = 0.96f,
            animationSpec = fastTween
        ) + fadeIn(
            animationSpec = fastTween
        )

    val tabExitTransition: ExitTransition = 
        scaleOut(
            targetScale = 1.02f,
            animationSpec = fastTween
        ) + fadeOut(
            animationSpec = fastTween
        )
}
