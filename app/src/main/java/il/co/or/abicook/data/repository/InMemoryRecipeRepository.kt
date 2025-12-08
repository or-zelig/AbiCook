package il.co.or.abicook.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import il.co.or.abicook.data.model.Recipe

object InMemoryRecipeRepository : RecipeRepository {

    private val _recipes = MutableLiveData<List<Recipe>>(emptyList())
    override val recipes: LiveData<List<Recipe>> = _recipes

    override fun addRecipe(recipe: Recipe) {
        val current = _recipes.value.orEmpty()
        _recipes.value = current + recipe
    }

    override fun getRecipe(id: String): Recipe? {
        return _recipes.value.orEmpty().firstOrNull { it.id == id }
    }
}
