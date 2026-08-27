package com.example.feature.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.di.AppContainer
import com.example.data.models.ContentDocument
import com.example.data.models.ContentNode
import com.example.data.repositories.PremiumRepository
import com.example.data.repositories.PremiumState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CourseUiState(
    val isLoading: Boolean = true,
    val premiumState: PremiumState = PremiumState.Loading,
    val rootNodes: List<ContentNode> = emptyList(),
    val currentNodes: List<ContentNode> = emptyList(),
    val navigationStack: List<ContentNode> = emptyList(),
    val error: String? = null
)

class CourseViewModel(
    private val courseId: String,
    private val startFolderId: String?,
    private val courseRepository: CourseRepository,
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    private val engine = CourseEngine()
    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState: StateFlow<CourseUiState> = _uiState.asStateFlow()
    
    private var rawDocsCache: List<ContentDocument> = emptyList()

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val actualCourseId = if (courseId == "DEFAULT") {
                    courseRepository.getDefaultCourseId() ?: "176"
                } else {
                    courseId
                }
                
                rawDocsCache = courseRepository.getCourseContent(actualCourseId)
                
                premiumRepository.getPremiumState().collectLatest { premiumState ->
                    val isPremium = premiumState is PremiumState.Premium
                    val roots = engine.decodeAndBuildTree(
                        courseId = actualCourseId,
                        rawDocs = rawDocsCache,
                        isUserPremium = isPremium
                    )

                    // Optional: Drill down to start folder if provided (only if stack is empty, i.e., first load)
                    var initialNodes = roots
                    var initialStack = _uiState.value.navigationStack.toMutableList()
                    
                    if (initialStack.isEmpty() && startFolderId != null) {
                        var startNode = roots.find { it.document.entityId == startFolderId }
                        if (startNode == null) {
                            startNode = roots.find { 
                                it.document.title.equals(startFolderId, ignoreCase = true) ||
                                it.document.title.contains(startFolderId, ignoreCase = true)
                            }
                        }

                        if (startNode != null && startNode.isFolder) {
                            Log.d("CourseEngine", "Opening subject: ${startNode.document.title} (${startNode.document.entityId})")
                            initialStack.add(startNode)
                            initialNodes = startNode.children
                        }
                    } else if (initialStack.isNotEmpty()) {
                        // Re-resolve the current folder's children from the newly decoded roots
                        var currentNodeList = roots
                        val newStack = mutableListOf<ContentNode>()
                        for (node in initialStack) {
                            val found = currentNodeList.find { it.document.entityId == node.document.entityId }
                            if (found != null) {
                                newStack.add(found)
                                currentNodeList = found.children
                            } else {
                                break // if path broken, stop
                            }
                        }
                        initialStack = newStack
                        initialNodes = currentNodeList
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        premiumState = premiumState,
                        rootNodes = roots,
                        currentNodes = initialNodes,
                        navigationStack = initialStack
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load course content"
                )
            }
        }
    }

    fun openFolder(node: ContentNode) {
        val currentStack = _uiState.value.navigationStack.toMutableList()
        currentStack.add(node)
        
        _uiState.value = _uiState.value.copy(
            navigationStack = currentStack,
            currentNodes = node.children
        )
    }

    fun navigateBack(): Boolean {
        val currentStack = _uiState.value.navigationStack.toMutableList()
        if (currentStack.isEmpty()) {
            return false // Handled by Activity/NavHost
        }
        
        currentStack.removeLast()
        val newCurrentNodes = if (currentStack.isEmpty()) {
            _uiState.value.rootNodes
        } else {
            currentStack.last().children
        }

        _uiState.value = _uiState.value.copy(
            navigationStack = currentStack,
            currentNodes = newCurrentNodes
        )
        return true
    }

    companion object {
        fun provideFactory(
            courseId: String,
            startFolderId: String?,
            container: AppContainer
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val courseRepo = CourseRepository(container.firestore)
                    val premiumRepo = com.example.data.repositories.PremiumRepository(container.firebaseAuth, container.firestore)
                    return CourseViewModel(courseId, startFolderId, courseRepo, premiumRepo) as T
                }
            }
    }
}
