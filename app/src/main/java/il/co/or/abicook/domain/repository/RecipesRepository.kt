package il.co.or.abicook.domain.repository

import il.co.or.abicook.domain.model.RecipePost

interface RecipesRepository {
    suspend fun getFeedRecipes(): List<RecipePost>
    suspend fun getMyRecipes(userId: String): List<RecipePost>
    suspend fun createRecipe(recipe: RecipePost)
}
