package com.example.core.navigation

import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import com.example.feature.wallet.WalletScreen
import com.example.feature.leaderboard.LeaderboardScreen
import com.example.feature.leaderboard.LeaderboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.di.AppContainer

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.feature.auth.AuthViewModel
import com.example.feature.auth.LoginScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Login : Screen("login", "Login", Icons.Filled.Home)
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Courses : Screen("courses", "Courses", Icons.AutoMirrored.Filled.MenuBook)
    object YouTube : Screen("youtube", "YouTube", Icons.Filled.PlayCircle)
object Chat : Screen("chat", "Chat", Icons.AutoMirrored.Filled.Chat)
    object Leaderboard : Screen("leaderboard", "Ranks", Icons.Filled.Star)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Wallet : Screen("wallet", "Wallet", Icons.Filled.AccountBalanceWallet)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Downloads : Screen("downloads", "Downloads", Icons.Filled.Download)
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Courses,
    Screen.Chat,
    Screen.Leaderboard
)

@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()
    val isOnline by appContainer.connectivityRepository.isOnline.collectAsState(initial = true)
    
    LaunchedEffect(isOnline) {
        if (!isOnline && appContainer.firebaseAuth.currentUser != null) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != Screen.Downloads.route && currentRoute?.startsWith("video_player") != true) {
                navController.navigate(Screen.Downloads.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else if (isOnline && appContainer.firebaseAuth.currentUser != null) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute == Screen.Downloads.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }


    Scaffold(
        containerColor = Color.Transparent,
        
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Only show bottom nav on main tabs
            val showBottomNav = BottomNavItems.any { it.route == currentDestination?.route }
            
            if (showBottomNav) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(68.dp)
                        .background(
                            color = Color(0xFF1E293B).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.9f else if (selected) 1.05f else 1f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
                        )
                        val activeBgAlpha by animateFloatAsState(targetValue = if (selected) 0.15f else 0f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = {
                                        if (!selected) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.scale(scale)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = Color.White.copy(alpha = activeBgAlpha),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.size(24.dp)) {
                                        when (screen.route) {
                                            "home" -> com.example.core.designsystem.HomeIcon3D(modifier = Modifier.fillMaxSize())
                                            "courses" -> com.example.core.designsystem.CourseIcon3D(modifier = Modifier.fillMaxSize())
                                            "chat" -> com.example.core.designsystem.ChatIcon3D(modifier = Modifier.fillMaxSize())
                                            "leaderboard" -> com.example.core.designsystem.LeaderboardIcon3D(modifier = Modifier.fillMaxSize())
                                            else -> Icon(
                                                screen.icon, 
                                                contentDescription = screen.title,
                                                tint = if (selected) Color.White else Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = screen.title,
                                    maxLines = 1,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                if (BottomNavItems.any { it.route == initialState.destination.route } && 
                    BottomNavItems.any { it.route == targetState.destination.route }) {
                    com.example.core.designsystem.HyperOsMotion.tabEnterTransition
                } else {
                    com.example.core.designsystem.HyperOsMotion.enterTransition
                }
            },
            exitTransition = {
                if (BottomNavItems.any { it.route == initialState.destination.route } && 
                    BottomNavItems.any { it.route == targetState.destination.route }) {
                    com.example.core.designsystem.HyperOsMotion.tabExitTransition
                } else {
                    com.example.core.designsystem.HyperOsMotion.exitTransition
                }
            },
            popEnterTransition = {
                if (BottomNavItems.any { it.route == initialState.destination.route } && 
                    BottomNavItems.any { it.route == targetState.destination.route }) {
                    com.example.core.designsystem.HyperOsMotion.tabEnterTransition
                } else {
                    com.example.core.designsystem.HyperOsMotion.popEnterTransition
                }
            },
            popExitTransition = {
                if (BottomNavItems.any { it.route == initialState.destination.route } && 
                    BottomNavItems.any { it.route == targetState.destination.route }) {
                    com.example.core.designsystem.HyperOsMotion.tabExitTransition
                } else {
                    com.example.core.designsystem.HyperOsMotion.popExitTransition
                }
            }
        ) {
            composable(Screen.Login.route) {
                val context = LocalContext.current
                val viewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.provideFactory(appContainer, context)
                )
                LoginScreen(viewModel = viewModel) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Home.route) {
                val viewModel: com.example.feature.home.HomeViewModel = viewModel(
                    factory = com.example.feature.home.HomeViewModel.provideFactory(appContainer)
                )
com.example.feature.home.HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSubject = { subjectId ->
                        // Navigate to DEFAULT course, pre-opening the chosen subject folder
                        val encodedId = java.net.URLEncoder.encode(subjectId, "UTF-8")
                        navController.navigate("course/DEFAULT?folderId=$encodedId")
                    },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                    onNavigateToWallet = { navController.navigate(Screen.Wallet.route) }
                )
            }
            composable(
                route = "course/{courseId}?folderId={folderId}",
                arguments = listOf(
                    androidx.navigation.navArgument("courseId") { type = androidx.navigation.NavType.StringType },
                    androidx.navigation.navArgument("folderId") { type = androidx.navigation.NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
                val folderId = backStackEntry.arguments?.getString("folderId")
                
                val viewModel: com.example.feature.course.CourseViewModel = viewModel(
                    factory = com.example.feature.course.CourseViewModel.provideFactory(courseId, folderId, appContainer)
                )
                
                com.example.feature.course.CourseScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPlayVideo = { url, title, lectureId, courseId ->
                        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
                        navController.navigate("video_player/$encodedUrl?title=$encodedTitle&lectureId=$lectureId&courseId=$courseId")
                    }
                )
            }
            composable(
                route = "video_player/{url}?title={title}&lectureId={lectureId}&courseId={courseId}",
                arguments = listOf(
                    androidx.navigation.navArgument("url") { type = androidx.navigation.NavType.StringType },
                    androidx.navigation.navArgument("title") { type = androidx.navigation.NavType.StringType; nullable = true },
                    androidx.navigation.navArgument("lectureId") { type = androidx.navigation.NavType.StringType; nullable = true; defaultValue = "" },
                    androidx.navigation.navArgument("courseId") { type = androidx.navigation.NavType.StringType; nullable = true; defaultValue = "" }
                )
            ) { backStackEntry ->
                val url = backStackEntry.arguments?.getString("url") ?: return@composable
                val title = backStackEntry.arguments?.getString("title") ?: "Lecture"
                val lectureId = backStackEntry.arguments?.getString("lectureId") ?: ""
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                
                com.example.feature.course.VideoPlayerScreen(
                    videoUrl = url,
                    videoTitle = title,
                    lectureId = lectureId,
                    courseId = courseId,
                    downloadRepository = appContainer.downloadRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Courses.route) {
                // Now directly renders the CourseScreen for DEFAULT
                val viewModel: com.example.feature.course.CourseViewModel = viewModel(
                    factory = com.example.feature.course.CourseViewModel.provideFactory("DEFAULT", null, appContainer)
                )
                
                com.example.feature.course.CourseScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPlayVideo = { url, title, lectureId, courseId ->
                        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
                        navController.navigate("video_player/$encodedUrl?title=$encodedTitle&lectureId=$lectureId&courseId=$courseId")
                    }
                )
            }
            composable(Screen.YouTube.route) {
                // YouTubeScreen(appContainer)
            }
            composable(Screen.Chat.route) {
                val viewModel: com.example.feature.chat.ChatViewModel = viewModel(
                    factory = com.example.feature.chat.ChatViewModel.provideFactory(appContainer)
                )
                com.example.feature.chat.ChatScreen(viewModel = viewModel)
            }
            composable(Screen.Leaderboard.route) {
                val viewModel: com.example.feature.leaderboard.LeaderboardViewModel = viewModel(
                    factory = com.example.feature.leaderboard.LeaderboardViewModel.provideFactory(appContainer)
                )
                LeaderboardScreen(viewModel = viewModel)
            }
            // Add subject detail, video player, etc.
            composable(Screen.Wallet.route) {
                val viewModel: com.example.feature.home.HomeViewModel = viewModel(
                    factory = com.example.feature.home.HomeViewModel.provideFactory(appContainer)
                )
                WalletScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                val viewModel: com.example.feature.home.HomeViewModel = viewModel(
                    factory = com.example.feature.home.HomeViewModel.provideFactory(appContainer)
                )
                com.example.feature.profile.ProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSignOut = {
                        appContainer.firebaseAuth.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                val viewModel: com.example.feature.settings.SettingsViewModel = viewModel(
                    factory = com.example.feature.settings.SettingsViewModel.provideFactory(appContainer)
                )
                com.example.feature.settings.SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Downloads.route) {
                val context = LocalContext.current
                val db = com.example.data.local.AppDatabase.getDatabase(context)
                val viewModel: com.example.feature.downloads.DownloadsViewModel = viewModel(
                    factory = com.example.feature.downloads.DownloadsViewModel.provideFactory(appContainer)
                )
                com.example.feature.downloads.DownloadsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPlayVideo = { url, title, localMediaId ->
                        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
                        navController.navigate("video_player/$encodedUrl?title=$encodedTitle")
                    }
                )
            }
        }
    }
}
