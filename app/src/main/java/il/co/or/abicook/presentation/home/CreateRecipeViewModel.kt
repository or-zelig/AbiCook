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

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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
                val user = auth.currentUser ?: throw Exception("Not logged in")
                val uid = user.uid

                // נביא username מה- users/{uid} אם קיים (מה-signUp שלך)
                val usernameFromDb = try {
                    firestore.collection("users").document(uid).get().await()
                        .getString("username")
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                } catch (_: Exception) {
                    null
                }

                val authorName = usernameFromDb
                    ?: user.displayName
                    ?: user.email
                    ?: "Unknown"

                val data = hashMapOf<String, Any?>(
                    "title" to title.trim(),
                    "description" to description.trim(),
                    "ingredientsSummary" to ingredientsSummary.trim(),
                    "stepsSummary" to stepsSummary.trim(),

                    "primaryCategory" to primaryCategory.trim(),
                    "categories" to categories,

                    "prepTimeMin" to prepTimeMin,
                    "cookTimeMin" to cookTimeMin,

                    "createdAtMillis" to System.currentTimeMillis(),

                    // ✅ זה הפתרון לבעיה שלך:
                    "authorId" to uid,
                    "authorName" to authorName,

                    "likes" to 0L,
                    "commentsCount" to 0L,

                    "imageUrl" to null
                )

                firestore.collection("recipes").add(data).await()

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
