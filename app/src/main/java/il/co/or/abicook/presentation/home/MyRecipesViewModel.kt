package il.co.or.abicook.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import il.co.or.abicook.data.repository.FirestoreMyRecipesRepository
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyRecipesUiState(
    val isLoading: Boolean = false,
    val recipes: List<RecipePost> = emptyList(),
    val error: String? = null
)

class MyRecipesViewModel(
    private val repo: FirestoreMyRecipesRepository = FirestoreMyRecipesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRecipesUiState())
    val uiState: StateFlow<MyRecipesUiState> = _uiState

    fun loadMyRecipes(categories: List<String>, sort: FeedSort) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Not logged in") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val data = repo.getMyRecipes(uid, categories, sort)
                _uiState.update { it.copy(isLoading = false, recipes = data) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }
}
