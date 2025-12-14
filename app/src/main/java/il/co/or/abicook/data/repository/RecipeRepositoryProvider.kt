package il.co.or.abicook.data.repository

object RecipeRepositoryProvider {
    val recipeRepository: RecipeRepository = FirestoreRecipeRepository()
}

