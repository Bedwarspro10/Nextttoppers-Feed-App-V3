package com.example.data.repositories

import com.example.data.models.AppUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar

class LeaderboardRepository(
    private val firestore: FirebaseFirestore
) {
    // Composite-index fallback pattern as specified in requirements
    fun getLeaderboard(period: String): Flow<List<AppUser>> = callbackFlow {
        val collectionRef = firestore.collection("leaderboard") // Web app uses 'leaderboard' collection for this? Actually, web app uses 'leaderboard' collection for cache or users? 
        // Wait, the web app queries `collection(db, "leaderboard")`. I will use the same collection.

        var query: Query = collectionRef.orderBy("xp", Query.Direction.DESCENDING).limit(100)

        if (period == "week") {
            val weekKey = getWeekKey()
            query = collectionRef
                .whereEqualTo("weekKey", weekKey)
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(100)
        } else if (period == "month") {
            val monthKey = getMonthKey()
            query = collectionRef
                .whereEqualTo("monthKey", monthKey)
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(100)
        }

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // FALLBACK if index is missing
                val fallbackQuery = collectionRef.orderBy("xp", Query.Direction.DESCENDING).limit(100)
                fallbackQuery.addSnapshotListener { fallbackSnap, fallbackError ->
                    if (fallbackError != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    if (fallbackSnap != null) {
                        val users = fallbackSnap.documents.mapNotNull { it.toObject(AppUser::class.java)?.copy(uid = it.id) }
                        trySend(users)
                    }
                }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val users = snapshot.documents.mapNotNull { it.toObject(AppUser::class.java)?.copy(uid = it.id) }
                trySend(users)
            }
        }

        awaitClose { listenerRegistration.remove() }
    }

    private fun getWeekKey(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.minimalDaysInFirstWeek = 4
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return "${year}-W${String.format("%02d", week)}"
    }

    private fun getMonthKey(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        return "${year}-${String.format("%02d", month)}"
    }
}
