import re

with open("app/src/main/java/com/example/data/models/Models.kt", "r") as f:
    content = f.read()

old_appuser = """data class AppUser(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val photoURL: String? = null,
    val role: String = "user",
    
    // Gamification (XP)
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val totalQuizzes: Int = 0,
    val totalCorrect: Int = 0,
    val perfectScores: Int = 0,
    val lecturesWatched: Int = 0,
    val pdfsRead: Int = 0,
    val avgScore: Double = 0.0,
    val quizzesBySubject: Map<String, Int> = emptyMap(),
    val achievements: List<String> = emptyList()
)"""

new_appuser = """data class AppUser(
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
)"""

content = content.replace(old_appuser, new_appuser)

with open("app/src/main/java/com/example/data/models/Models.kt", "w") as f:
    f.write(content)
