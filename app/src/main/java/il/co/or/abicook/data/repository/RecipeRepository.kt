package il.co.or.abicook.data.repository

import androidx.lifecycle.LiveData
import il.co.or.abicook.data.model.Recipe

interface RecipeRepository {
    val recipes: LiveData<List<Recipe>>

    fun addRecipe(
        recipe: Recipe,
        onResult: (success: Boolean, errorMessage: String?) -> Unit = { _, _ -> }
    )

    fun getRecipe(id: String): Recipe?
}
