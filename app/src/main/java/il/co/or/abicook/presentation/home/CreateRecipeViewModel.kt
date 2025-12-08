package il.co.or.abicook.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import il.co.or.abicook.data.model.Recipe
import il.co.or.abicook.data.repository.RecipeRepositoryProvider
import java.util.UUID


enum class CreateRecipeStep {
    BASIC,
    INGREDIENTS,
    STEPS,
    SUMMARY
}

data class CreateRecipeUiState(
    val step: CreateRecipeStep = CreateRecipeStep.BASIC,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class CreateRecipeViewModel : ViewModel() {

    private val _uiState = MutableLiveData(CreateRecipeUiState())
    val uiState: LiveData<CreateRecipeUiState> = _uiState
    private val repository = RecipeRepositoryProvider.recipeRepository


    fun goToIngredients() {
        _uiState.value = CreateRecipeUiState(step = CreateRecipeStep.INGREDIENTS)
    }

    fun goToSteps() {
        _uiState.value = CreateRecipeUiState(step = CreateRecipeStep.STEPS)
    }

    fun goToSummary() {
        _uiState.value = CreateRecipeUiState(step = CreateRecipeStep.SUMMARY)
    }

    fun previous() {
        val current = _uiState.value?.step ?: CreateRecipeStep.BASIC
        val prev = when (current) {
            CreateRecipeStep.BASIC -> CreateRecipeStep.BASIC
            CreateRecipeStep.INGREDIENTS -> CreateRecipeStep.BASIC
            CreateRecipeStep.STEPS -> CreateRecipeStep.INGREDIENTS
            CreateRecipeStep.SUMMARY -> CreateRecipeStep.STEPS
        }
        _uiState.value = CreateRecipeUiState(step = prev)
    }

    fun publishRecipe(
        title: String,
        description: String,
        ingredientsSummary: String,
        stepsSummary: String
    ) {
        // מציין שאנחנו מתחילים "עבודה"
        _uiState.value = _uiState.value?.copy(
            isLoading = true,
            error = null
        )

        val recipe = Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            ingredientsSummary = ingredientsSummary,
            stepsSummary = stepsSummary,
            createdAtMillis = System.currentTimeMillis()
        )

        // שמירה ב-InMemory repository
        repository.addRecipe(recipe)

        // סימון הצלחה
        _uiState.value = _uiState.value?.copy(
            isLoading = false,
            success = true
        )
    }


    fun resetSuccess() {
        _uiState.value = CreateRecipeUiState(step = CreateRecipeStep.BASIC)
    }
}
