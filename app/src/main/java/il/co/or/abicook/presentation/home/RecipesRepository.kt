package il.co.or.abicook.presentation.home

interface RecipesRepository {
    suspend fun getAllRecipes(): List<RecipeUiItem>
}
