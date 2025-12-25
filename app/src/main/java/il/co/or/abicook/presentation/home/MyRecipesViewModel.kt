package il.co.or.abicook.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import il.co.or.abicook.data.repository.FirestoreMyRecipesRepository
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            _uiState.value = MyRecipesUiState(error = "Not logged in")
            return
        }

        viewModelScope.launch {
            _uiState.value = MyRecipesUiState(isLoading = true)
            try {

                val data = repo.getMyRecipes(uid, categories, sort)
                _uiState.value = MyRecipesUiState(recipes = data)
            } catch (e: Exception) {
                _uiState.value = MyRecipesUiState(error = e.message ?: "Unknown error")
            }
        }
    }
}
