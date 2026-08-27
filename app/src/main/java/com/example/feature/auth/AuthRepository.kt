package com.example.feature.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(private val auth: FirebaseAuth, private val context: Context) {
    
    // In production, this client ID should come from BuildConfig / R.string.default_web_client_id
    // But since we are building it in AI Studio, we need the server client ID for Google Sign In.
    // The web app has it configured via Firebase. 
    // We will use a placeholder or read it from string resources if available.
    
    suspend fun signInWithGoogle(): Boolean {
        val credentialManager = CredentialManager.create(context)
        
        // Generate a nonce
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        // Needs the actual web client ID from Firebase console. 
        // We will pull from context resources "default_web_client_id" dynamically.
        val webClientId = context.getString(
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        )

        if (webClientId.isEmpty()) return false

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(authCredential).await()
                true
            } else {
                throw Exception("Unexpected credential type: ${credential.type}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
}
