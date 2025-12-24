package il.co.or.abicook.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateRecipeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val publishSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun publishRecipe(
        title: String,
        description: String,
        ingredientsSummary: String,
        stepsSummary: String,
        primaryCategory: String,
        categories: List<String>,
        prepTimeMin: Int,
        cookTimeMin: Int
    ) {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)

            try {
                val userId = auth.currentUser?.uid.orEmpty()

                val doc = mapOf(
                    "title" to title.trim(),
                    "description" to description.trim(),
                    "ingredientsSummary" to ingredientsSummary.trim(),
                    "stepsSummary" to stepsSummary.trim(),

                    "primaryCategory" to primaryCategory.trim(),
                    "categories" to categories,
                    "prepTimeMin" to prepTimeMin,
                    "cookTimeMin" to cookTimeMin,

                    "imageUrl" to null,
                    "createdAtMillis" to System.currentTimeMillis(),
                    "authorId" to userId,
                    "authorName" to if (userId.isBlank()) "Anonymous" else "User",

                    "likes" to 0,
                    "commentsCount" to 0
                )

                firestore.collection("recipes").add(doc).await()

                _uiState.value = UiState(publishSuccess = true)
            } catch (e: Exception) {
                _uiState.value = UiState(error = e.message ?: "Unknown error")
            }
        }
    }

    fun onHandledSuccess() {
        _uiState.value = _uiState.value.copy(publishSuccess = false)
    }

    fun onHandledError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
