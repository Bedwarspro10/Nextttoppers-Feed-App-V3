package com.example.feature.leaderboard

object LevelUtils {

    fun getLevelTitle(level: Int): String {
        return when {
            level == 0 -> "Newcomer"
            level < 5 -> "Newbie"
            level < 10 -> "Learner"
            level < 15 -> "Rising Star"
            level < 20 -> "Focused Student"
            level < 25 -> "Smart Learner"
            level < 30 -> "Quiz Warrior"
            level < 40 -> "Knowledge Seeker"
            level < 50 -> "Academic Beast"
            level < 60 -> "Study Machine"
            level < 70 -> "Study Legend"
            level < 80 -> "Elite Performer"
            level < 90 -> "Mastermind"
            level < 95 -> "Grandmaster"
            level < 100 -> "Ultimate Topper"
            else -> "Hall of Fame"
        }
    }

    // Since the exact formula from the website is unavailable,
    // we use a safe fallback progressive curve.
    fun getXpForLevel(level: Int): Int {
        if (level <= 1) return 0
        return (level - 1) * 500
    }
}
