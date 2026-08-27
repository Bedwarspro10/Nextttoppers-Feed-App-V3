package com.example.feature.course

import android.util.Log
import com.example.data.models.ContentDocument
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CourseRepository(private val firestore: FirebaseFirestore) {

    suspend fun getDefaultCourseId(): String? = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("courses").limit(1).get().await()
            snapshot.documents.firstOrNull()?.id ?: "176"
        } catch (e: Exception) {
            "176"
        }
    }

    suspend fun getCourseContent(courseId: String): List<ContentDocument> = withContext(Dispatchers.IO) {
        try {
            Log.d("CourseEngine", "Fetching content for courseId: $courseId")
            val snapshot = firestore.collection("courses")
                .document(courseId)
                .collection("content")
                .get()
                .await()

            Log.d("CourseEngine", "Total documents fetched: ${snapshot.size()}")

            snapshot.documents.mapNotNull { doc ->
                val entityId = doc.getString("entityId") ?: doc.id
                val parentId = doc.getString("parentId")
                val title = doc.getString("title") ?: "Untitled"
                
                val fileUrl = doc.getString("fileUrl")
                val pdfUrl = doc.getString("pdfUrl")
                
                val type = doc.getString("type") ?: ""
                val status = doc.getString("status") ?: ""
                val updatedAt = doc.getLong("updatedAt") ?: 0L

                ContentDocument(
                    courseId = courseId,
                    entityId = entityId,
                    fileUrl = fileUrl,
                    parentId = parentId,
                    pdfUrl = pdfUrl,
                    status = status,
                    title = title,
                    type = type,
                    updatedAt = updatedAt
                )
            }
        } catch (e: Exception) {
            Log.e("CourseEngine", "Error fetching content for course $courseId", e)
            emptyList()
        }
    }
}
