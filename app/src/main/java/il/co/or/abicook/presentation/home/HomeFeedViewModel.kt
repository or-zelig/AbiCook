package il.co.or.abicook.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedRepository
import kotlinx.coroutines.launch

data class HomeFeedUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val posts: List<RecipePost> = emptyList()
)

class HomeFeedViewModel(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(HomeFeedUiState())
    val uiState: LiveData<HomeFeedUiState> = _uiState

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = HomeFeedUiState(isLoading = true)

            try {
                val posts = feedRepository.getHomeFeed()
                _uiState.value = HomeFeedUiState(
                    isLoading = false,
                    posts = posts
                )
            } catch (e: Exception) {
                _uiState.value = HomeFeedUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load feed"
                )
            }
        }
    }
}
