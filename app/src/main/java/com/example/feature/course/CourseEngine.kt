package com.example.feature.course

import android.util.Log
import com.example.data.models.ContentDocument
import com.example.data.models.ContentNode

class CourseEngine {
    
    fun decodeAndBuildTree(
        courseId: String,
        rawDocs: List<ContentDocument>,
        isUserPremium: Boolean
    ): List<ContentNode> {
        Log.d("CourseEngine", "--- DECODING START ---")
        Log.d("CourseEngine", "Course ID: $courseId")
        Log.d("CourseEngine", "Total documents to decode: ${rawDocs.size}")

        var totalResolvedPdfs = 0
        var totalResolvedLectures = 0
        var totalPending = 0

        // 1. Deduplicate by entityId and prefer valid resolvedUrl
        val deduplicated = mutableMapOf<String, ContentDocument>()
        for (doc in rawDocs) {
            val existing = deduplicated[doc.entityId]
            if (existing == null) {
                deduplicated[doc.entityId] = doc
            } else {
                val existingHasUrl = !existing.resolvedUrl.isNullOrEmpty()
                val newHasUrl = !doc.resolvedUrl.isNullOrEmpty()
                
                if (!existingHasUrl && newHasUrl) {
                    deduplicated[doc.entityId] = doc
                } else if (existingHasUrl == newHasUrl && doc.updatedAt > existing.updatedAt) {
                    deduplicated[doc.entityId] = doc
                }
            }
        }

        // 2. Build Hierarchy using parentId matching entityId
        val nodes = deduplicated.values.associate { it.entityId to ContentNode(it) }
        val roots = mutableListOf<ContentNode>()

        nodes.values.forEach { node ->
            val parentId = node.document.parentId
            if (parentId.isNullOrEmpty() || parentId == "null") {
                roots.add(node)
            } else {
                val parent = nodes[parentId]
                if (parent != null) {
                    parent.children.add(node)
                } else {
                    // Parent not found, treat as root so it isn't lost
                    roots.add(node)
                }
            }
        }

        Log.d("CourseEngine", "Root nodes detected: ${roots.size}")
        roots.forEach { root ->
            Log.d("CourseEngine", "Root [${root.document.entityId}] ${root.document.title} - Direct children: ${root.children.size}")
        }

        // 3. Apply Premium Logic, Filtering, and Sort
        var recursiveResourceCount = 0

        fun processNode(node: ContentNode, isSubjectLocked: Boolean) {
            // Remove files that do not have a URL
            node.children.removeAll { it.isFile && it.document.resolvedUrl.isNullOrEmpty() }

            val doc = node.document
            
            if (doc.status.equals("pending", ignoreCase = true)) {
                totalPending++
            } else if (node.isResolved && node.isFile) {
                val url = doc.resolvedUrl ?: ""
                val isLecture = url.contains(".m3u8") || url.contains(".mp4") || url.contains("youtube")
                
                if (isLecture) {
                    totalResolvedLectures++
                } else {
                    totalResolvedPdfs++
                }

                // Premium logic: PDFs are free. HLS/video lectures locked if subject locked & user not premium.
                node.isPremiumLocked = if (isLecture && isSubjectLocked && !isUserPremium) {
                    true
                } else {
                    false
                }
            }

            recursiveResourceCount++
            node.children.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.document.title })
            
            // Recursively process children
            node.children.forEach { processNode(it, isSubjectLocked) }
        }

        roots.removeAll { it.isFile && it.document.resolvedUrl.isNullOrEmpty() }
        roots.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.document.title })
        
        roots.forEach { root -> 
            val title = root.document.title.lowercase()
            val entityId = root.document.entityId.lowercase()
            
            // English and Hindi are fully free (PDFs + Lectures). Others (Maths, Science, SST) have locked lectures.
            val isFreeSubject = title.contains("english") || title.contains("hindi") ||
                                entityId.contains("english") || entityId.contains("hindi")
            
            processNode(root, isSubjectLocked = !isFreeSubject) 
        }

        Log.d("CourseEngine", "Number of recursively discovered resources: $recursiveResourceCount")
        Log.d("CourseEngine", "Number of resolved PDFs: $totalResolvedPdfs")
        Log.d("CourseEngine", "Number of resolved lectures: $totalResolvedLectures")
        Log.d("CourseEngine", "Number of pending resources: $totalPending")
        Log.d("CourseEngine", "--- DECODING COMPLETE ---")

        return roots
    }
}
