package il.co.or.abicook.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import il.co.or.abicook.data.repository.RecipeRepositoryProvider
import il.co.or.abicook.domain.model.RecipePost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateRecipeViewModel : ViewModel() {

    // ✅ לא צריך ctor עם פרמטרים (ככה ViewModelProvider(this)[...] עובד בלי Factory)
    private val recipeRepository = RecipeRepositoryProvider.provideRecipeRepository()

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

            val result = recipeRepository.createRecipe(
                RecipePost(
                    id = "",
                    title = title.trim(),
                    description = description.trim(),
                    ingredientsSummary = ingredientsSummary.trim(),
                    stepsSummary = stepsSummary.trim(),
                    primaryCategory = primaryCategory.trim(),
                    categories = categories,
                    prepTimeMin = prepTimeMin,
                    cookTimeMin = cookTimeMin
                )
            )

            result.fold(
                onSuccess = {
                    _uiState.value = UiState(publishSuccess = true)
                },
                onFailure = { e ->
                    _uiState.value = UiState(error = e.message ?: "Unknown error")
                }
            )
        }
    }

    fun onHandledSuccess() {
        _uiState.value = _uiState.value.copy(publishSuccess = false)
    }

    fun onHandledError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
