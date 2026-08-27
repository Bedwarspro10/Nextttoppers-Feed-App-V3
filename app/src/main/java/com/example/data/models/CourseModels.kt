package com.example.data.models

data class ContentDocument(
    val courseId: String = "",
    val entityId: String = "",
    val fileUrl: String? = null,
    val parentId: String? = null,
    val pdfUrl: String? = null,
    val status: String = "",
    val title: String = "",
    val type: String = "",
    val updatedAt: Long = 0L
) {
    val resolvedUrl: String? 
        get() = if (!pdfUrl.isNullOrEmpty()) pdfUrl else if (!fileUrl.isNullOrEmpty()) fileUrl else null
}

data class ContentNode(
    val document: ContentDocument,
    val children: MutableList<ContentNode> = mutableListOf(),
    var isPremiumLocked: Boolean = false
) {
    val isFolder: Boolean
        get() = document.type.equals("folder", ignoreCase = true) || 
                document.type.equals("chapter", ignoreCase = true) ||
                document.type.equals("subject", ignoreCase = true) ||
                document.type.isEmpty() && children.isNotEmpty()

    val isFile: Boolean
        get() = document.type.equals("file", ignoreCase = true)

    val isResolved: Boolean
        get() = document.status.equals("resolved", ignoreCase = true)
}
