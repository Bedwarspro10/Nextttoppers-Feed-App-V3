import re

with open("app/src/main/java/com/example/data/repositories/UserRepository.kt", "r") as f:
    content = f.read()

imports = """import com.example.data.models.AppUser
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
"""

if "import kotlinx.coroutines.flow.Flow" not in content:
    content = content.replace("import com.example.data.models.AppUser", imports.split("import com.example.data.models.AppUser")[1] + "import com.example.data.models.AppUser")

new_functions = """

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
"""

if "getUserFlow()" not in content:
    content = content.replace("suspend fun updateXP(amount: Int) {", new_functions + "\n    suspend fun updateXP(amount: Int) {")

with open("app/src/main/java/com/example/data/repositories/UserRepository.kt", "w") as f:
    f.write(content)
