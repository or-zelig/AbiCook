package il.co.or.abicook.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedRepository
import kotlinx.coroutines.launch

class RecipeFeedViewModel(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _posts = MutableLiveData<List<RecipePost>>()
    val posts: LiveData<List<RecipePost>> = _posts

    fun loadFeed() {
        viewModelScope.launch {
            _posts.value = feedRepository.getHomeFeed()
        }
    }
}
