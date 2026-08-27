package com.example.data.repositories


import com.example.data.models.CoinWalletData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import com.example.data.models.AppUser

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun getOrCreateUser(): AppUser? = withContext(Dispatchers.IO) {
        val currentUser = auth.currentUser ?: return@withContext null
        val userRef = firestore.collection("users").document(currentUser.uid)
        
        val snapshot = userRef.get().await()
        if (snapshot.exists()) {
            val data = snapshot.toObject(AppUser::class.java)
            data?.copy(uid = currentUser.uid)
        } else {
            val newUser = AppUser(
                uid = currentUser.uid,
                displayName = currentUser.displayName,
                email = currentUser.email,
                photoURL = currentUser.photoUrl?.toString()
            )
            userRef.set(newUser).await()
            newUser
        }
    }

    /**
     * Fetches a user's public profile by uid. Used to resolve avatar/display name
     * for the other participant in a private chat. Read-only, additive helper —
     * does not change any existing collection, field, or query.
     */
    suspend fun getUserById(uid: String): AppUser? = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext null
        val snapshot = firestore.collection("users").document(uid).get().await()
        if (snapshot.exists()) {
            snapshot.toObject(AppUser::class.java)?.copy(uid = uid)
        } else {
            null
        }
    }

    suspend fun getWalletData(): CoinWalletData = withContext(Dispatchers.IO) {
        val currentUser = auth.currentUser ?: return@withContext CoinWalletData()
        val walletRef = firestore.collection("coinWallet").document(currentUser.uid)
        val snapshot = walletRef.get().await()
        if (snapshot.exists()) {
            snapshot.toObject(CoinWalletData::class.java) ?: CoinWalletData()
        } else {
            CoinWalletData()
        }
    }
    
    

    fun getUserFlow(): Flow<AppUser?> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val userRef = firestore.collection("users").document(currentUser.uid)
        val listener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.toObject(AppUser::class.java)
                trySend(data?.copy(uid = currentUser.uid))
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getWalletFlow(): Flow<CoinWalletData> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(CoinWalletData())
            close()
            return@callbackFlow
        }
        val walletRef = firestore.collection("coinWallet").document(currentUser.uid)
        val listener = walletRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(CoinWalletData())
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(CoinWalletData::class.java) ?: CoinWalletData())
            } else {
                trySend(CoinWalletData())
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateXP(amount: Int) {
        val currentUser = auth.currentUser ?: return
        val userRef = firestore.collection("users").document(currentUser.uid)
        userRef.update("xp", FieldValue.increment(amount.toLong())).await()
    }
}
