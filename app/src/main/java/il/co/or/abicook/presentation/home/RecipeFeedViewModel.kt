package il.co.or.abicook.presentation.home

import androidx.lifecycle.*
import kotlinx.coroutines.launch

enum class SortMode { NEWEST, MOST_LIKED }

data class FeedFilters(
    val categories: Set<String>,
    val sortMode: SortMode,
    val maxTotalTimeMinutes: Int? // null = no limit
)

sealed class FeedUiState {
    object Idle : FeedUiState()
    object Loading : FeedUiState()
    data class Results(val items: List<RecipeUiItem>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

data class RecipeUiItem(
    val id: String,
    val title: String,
    val category: String,
    val likes: Int,
    val prepTime: Int,
    val cookTime: Int,
    val createdAtMillis: Long
) {
    val totalTime: Int get() = prepTime + cookTime
}

class RecipeFeedViewModel(
    private val repo: RecipesRepository // תואם לריפו שלך (תכף למטה)
) : ViewModel() {

    private val _state = MutableLiveData<FeedUiState>(FeedUiState.Idle)
    val state: LiveData<FeedUiState> = _state

    // נשמור את כל המתכונים פעם אחת ואז פילטר מקומי (פשוט ויציב כרגע)
    private var cachedAll: List<RecipeUiItem> = emptyList()

    fun loadAllIfNeeded() {
        if (cachedAll.isNotEmpty()) return
        viewModelScope.launch {
            _state.value = FeedUiState.Loading
            try {
                cachedAll = repo.getAllRecipes()
                _state.value = FeedUiState.Idle
            } catch (e: Exception) {
                _state.value = FeedUiState.Error(e.message ?: "Failed to load recipes")
            }
        }
    }

    fun applyFilters(filters: FeedFilters) {
        val base = cachedAll

        val filtered = base
            .asSequence()
            .filter { it.category in filters.categories }
            .filter { filters.maxTotalTimeMinutes == null || it.totalTime <= filters.maxTotalTimeMinutes }
            .toList()

        val sorted = when (filters.sortMode) {
            SortMode.NEWEST -> filtered.sortedByDescending { it.createdAtMillis }
            SortMode.MOST_LIKED -> filtered.sortedByDescending { it.likes }
        }

        _state.value = FeedUiState.Results(sorted)
    }

    fun backToFilters() {
        _state.value = FeedUiState.Idle
    }
}
