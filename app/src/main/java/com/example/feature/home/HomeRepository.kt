package com.example.feature.home

import com.example.data.models.Announcement
import com.example.data.models.Banner
import com.example.data.models.Subject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeRepository(private val firestore: FirebaseFirestore) {

    suspend fun getActiveBanners(): List<Banner> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("banners")
                .whereEqualTo("active", true)
                .get()
                .await()
            snapshot.documents.mapNotNull { 
                it.toObject(Banner::class.java)?.copy(id = it.id) 
            }.sortedByDescending { it.priority }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLatestAnnouncements(): List<Announcement> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .await()
            snapshot.documents.mapNotNull { 
                it.toObject(Announcement::class.java)?.copy(id = it.id) 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSubjects(): List<Subject> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("subjects")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()
            val subjects = snapshot.documents.mapNotNull { 
                it.toObject(Subject::class.java)?.copy(id = it.id) 
            }
            if (subjects.isNotEmpty()) {
                return@withContext subjects
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Fallback mock subjects if collection doesn't exist, is empty, or errors
        listOf(
            Subject("maths", "Mathematics", "maths", 1),
            Subject("science", "Science", "science", 2),
            Subject("sst", "Social Science (SST)", "sst", 3),
            Subject("english", "English", "english", 4),
            Subject("hindi", "Hindi", "hindi", 5)
        )
    }
}
