package il.co.or.abicook.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import il.co.or.abicook.domain.model.RecipePost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FeedUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val recipes: List<RecipePost> = emptyList()
)

class RecipeFeedViewModel(
    private val repo: FirestoreRecipeDataRepository = FirestoreRecipeDataRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = FeedUiState(isLoading = true)
            try {
                val items = repo.loadFeed()
                _uiState.value = FeedUiState(isLoading = false, recipes = items)
            } catch (t: Throwable) {
                _uiState.value = FeedUiState(isLoading = false, error = t.message ?: "Unknown error")
            }
        }
    }
}
