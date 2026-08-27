package com.example.data.models

import com.google.firebase.Timestamp
import com.squareup.moshi.JsonClass

data class AppUser(
    val uid: String = "",
    val name: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val photoURL: String? = null,
    val role: String = "user",
    val isPremium: Boolean = false,
    
    // Gamification (XP)
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val totalQuizzes: Int = 0,
    val totalCorrect: Int = 0,
    val totalScore: Int = 0,
    val perfectScores: Int = 0,
    val lecturesWatched: Int = 0,
    val pdfsRead: Int = 0,
    val weeksActive: Int = 0,
    val monthsActive: Int = 0,
    val avgScore: Double = 0.0,
    val quizzesBySubject: Map<String, Int> = emptyMap(),
    val achievements: List<String> = emptyList(),
    val weekKey: String? = null,
    val monthKey: String? = null,
    val updatedAt: Timestamp? = null
)

data class CoinWalletData(
    val balance: Int = 0,
    val lifetimeEarned: Int = 0,
    val lifetimeRedeemed: Int = 0,
    val monthlyRedeems: Map<String, Int> = emptyMap(),
    val lastResetMonth: String = "",
    val loginStreak: Int = 0,
    val lastLoginDate: String = ""
)

data class Announcement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val createdAt: Timestamp? = null
)

data class Banner(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val ctaText: String = "",
    val url: String = "",
    val bgGradient: String = "", // e.g. "from-blue-600 to-indigo-600"
    val active: Boolean = true,
    val priority: Int = 0
)

data class Subject(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val order: Int = 0
)

data class CourseContentItem(
    val id: String = "",
    val entityId: String = "",
    val title: String = "",
    val type: String = "", // "lecture", "file", "test"
    val isPremium: Boolean = false,
    val subject: String = "",
    val folderId: String = "",
    
    // For files
    val link: String = "",
    val category: String = "",
    
    // For lectures
    val videoUrl: String = "",
    val thumbnail: String = "",
    
    // For tests
    val durationMins: Int = 0,
    val rewardCoins: Int = 0,
    val rewardXp: Int = 0,
    
    val createdAt: Timestamp? = null,
    val order: Int = 0
)

data class CourseFolder(
    val id: String = "",
    val name: String = "",
    val subject: String = "",
    val order: Int = 0,
    val items: List<CourseContentItem> = emptyList()
)
