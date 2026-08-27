package com.example.data.repositories

import com.example.data.models.CourseContentItem
import com.example.data.models.CourseFolder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseRepository(private val firestore: FirebaseFirestore) {

    suspend fun getCourseContent(subjectId: String): List<CourseFolder> = withContext(Dispatchers.IO) {
        // 1. Fetch folders
        val foldersSnap = firestore.collection("lecture_folders")
            .whereEqualTo("subject", subjectId)
            .orderBy("order", Query.Direction.ASCENDING)
            .get().await()

        val rawFolders = foldersSnap.documents.map { doc ->
            CourseFolder(
                id = doc.id,
                name = doc.getString("name") ?: "",
                subject = subjectId,
                order = doc.getLong("order")?.toInt() ?: 0
            )
        }

        // 2. Fetch items (Files, Lectures, Tests)
        val filesSnap = firestore.collection("files")
            .whereEqualTo("subject", subjectId)
            .get().await()

        val lecturesSnap = firestore.collection("lectures")
            .whereEqualTo("subject", subjectId)
            .get().await()

        val testsSnap = try {
            firestore.collection("tests")
                .whereEqualTo("subject", subjectId)
                .get().await()
        } catch (e: Exception) { null } // Tests might not exist yet

        val allItems = mutableListOf<CourseContentItem>()

        filesSnap.documents.forEach { doc ->
            allItems.add(
                CourseContentItem(
                    id = doc.id,
                    type = "file",
                    title = doc.getString("name") ?: "",
                    isPremium = doc.getBoolean("isPremium") ?: false,
                    subject = subjectId,
                    folderId = doc.getString("folderId") ?: "",
                    link = doc.getString("link") ?: "",
                    category = doc.getString("category") ?: "",
                    order = doc.getLong("order")?.toInt() ?: 0,
                    createdAt = doc.getTimestamp("createdAt")
                )
            )
        }

        lecturesSnap.documents.forEach { doc ->
            allItems.add(
                CourseContentItem(
                    id = doc.id,
                    type = "lecture",
                    title = doc.getString("title") ?: "",
                    isPremium = doc.getBoolean("isPremium") ?: false,
                    subject = subjectId,
                    folderId = doc.getString("folderId") ?: "",
                    videoUrl = doc.getString("videoUrl") ?: "",
                    thumbnail = doc.getString("thumbnail") ?: "",
                    order = doc.getLong("order")?.toInt() ?: 0,
                    createdAt = doc.getTimestamp("createdAt")
                )
            )
        }

        testsSnap?.documents?.forEach { doc ->
            allItems.add(
                CourseContentItem(
                    id = doc.id,
                    type = "test",
                    title = doc.getString("title") ?: "",
                    isPremium = doc.getBoolean("isPremium") ?: false,
                    subject = subjectId,
                    folderId = doc.getString("folderId") ?: "",
                    durationMins = doc.getLong("durationMins")?.toInt() ?: 0,
                    rewardCoins = doc.getLong("rewardCoins")?.toInt() ?: 0,
                    rewardXp = doc.getLong("rewardXp")?.toInt() ?: 0,
                    order = doc.getLong("order")?.toInt() ?: 0,
                    createdAt = doc.getTimestamp("createdAt")
                )
            )
        }

        // 3. Group by folderId
        val folderItemsMap = allItems.groupBy { it.folderId }

        val rootItems = folderItemsMap[""] ?: emptyList()
        val sortedRootItems = rootItems.sortedBy { it.order }

        val foldersWithItems = rawFolders.map { folder ->
            folder.copy(
                items = (folderItemsMap[folder.id] ?: emptyList()).sortedBy { it.order }
            )
        }

        // Return a mock root folder + actual folders
        val result = mutableListOf<CourseFolder>()
        
        if (sortedRootItems.isNotEmpty()) {
            result.add(CourseFolder(id = "root", name = "General", subject = subjectId, items = sortedRootItems))
        }
        
        result.addAll(foldersWithItems)
        
        return@withContext result
    }
}
