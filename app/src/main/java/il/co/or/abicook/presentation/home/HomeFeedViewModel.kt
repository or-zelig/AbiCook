package il.co.or.abicook.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeFeedUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val posts: List<RecipePost> = emptyList()
)

class HomeFeedViewModel(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(HomeFeedUiState(isLoading = true))
    val uiState: LiveData<HomeFeedUiState> = _uiState

    private var observeJob: Job? = null

    fun startObservingFeed() {
        if (observeJob != null) return // שלא נפתח מאזין כפול

        observeJob = viewModelScope.launch {
            _uiState.value = HomeFeedUiState(isLoading = true)

            feedRepository.observeHomeFeed()
                .catch { e ->
                    _uiState.value = HomeFeedUiState(
                        isLoading = false,
                        error = e.message ?: "Failed to load feed"
                    )
                }
                .collect { posts ->
                    _uiState.value = HomeFeedUiState(
                        isLoading = false,
                        posts = posts
                    )

                    android.util.Log.d("FEED_VM", "posts size=${posts.size}")
                }
        }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}
