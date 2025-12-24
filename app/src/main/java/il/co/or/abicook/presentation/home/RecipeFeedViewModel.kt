package il.co.or.abicook.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import il.co.or.abicook.data.repository.FirestoreFeedRepository
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeFeedUiState(
    val isLoading: Boolean = false,
    val recipes: List<RecipePost> = emptyList(),
    val error: String? = null
)

class RecipeFeedViewModel : ViewModel() {

    private val repo = FirestoreFeedRepository()

    private val _uiState = MutableStateFlow(RecipeFeedUiState())
    val uiState: StateFlow<RecipeFeedUiState> = _uiState.asStateFlow()

    fun load(
        categories: List<String> = emptyList(),
        sort: FeedSort = FeedSort.NEWEST
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val posts = repo.getFeed(categories, sort)
                _uiState.update { it.copy(isLoading = false, recipes = posts) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }
}
