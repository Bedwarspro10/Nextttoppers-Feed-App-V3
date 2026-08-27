package com.example.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class PremiumState {
    object Loading : PremiumState()
    object Free : PremiumState()
    object Premium : PremiumState()
}

class PremiumRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private fun getMillis(snapshot: com.google.firebase.firestore.DocumentSnapshot, field: String): Long? {
        val value = snapshot.get(field)
        return when (value) {
            is Long -> value
            is Double -> value.toLong()
            is com.google.firebase.Timestamp -> value.seconds * 1000 + value.nanoseconds / 1000000
            else -> null
        }
    }

    fun getPremiumState(): Flow<PremiumState> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(PremiumState.Free)
            close()
            return@callbackFlow
        }

        // First check if the user is an admin from the users collection
        var isAdmin = false
        val userListener = firestore.collection("users").document(currentUser.uid)
            .addSnapshotListener { userSnapshot, _ ->
                isAdmin = userSnapshot?.getString("role") == "admin"
                if (isAdmin) {
                    trySend(PremiumState.Premium)
                }
            }

        // Listen to premiumUsers/{uid}
        val premiumListener = firestore.collection("premiumUsers").document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!isAdmin) trySend(PremiumState.Free)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val isPremiumFlag = snapshot.getBoolean("isPremium")
                    val flagActive = isPremiumFlag != false // null is true, true is true, false is false
                    
                    val expiryTime = getMillis(snapshot, "expiryTime")
                    val expiresAt = getMillis(snapshot, "expiresAt")
                    val expiryDate = expiryTime ?: expiresAt
                    
                    val currentTime = System.currentTimeMillis()
                    val timeActive = expiryDate != null && expiryDate > currentTime
                    
                    val finalIsPremium = flagActive && timeActive
                    
                    if (finalIsPremium || isAdmin) {
                        trySend(PremiumState.Premium)
                    } else {
                        trySend(PremiumState.Free)
                    }
                } else {
                    if (!isAdmin) {
                        trySend(PremiumState.Free)
                    }
                }
            }

        awaitClose {
            userListener.remove()
            premiumListener.remove()
        }
    }
}
