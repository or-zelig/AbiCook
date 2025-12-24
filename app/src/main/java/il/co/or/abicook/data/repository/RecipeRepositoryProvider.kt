package il.co.or.abicook.data.repository

import il.co.or.abicook.data.repository.firestore.FirestoreRecipeDataRepository
import il.co.or.abicook.presentation.home.RecipesRepository

object RecipesRepositoryProvider {
    fun provideRecipesRepository(): RecipesRepository {
        // כאן אתה מחזיר את מה שבאמת מושך מ-Firestore
        return FirestoreRecipeDataRepository()
    }
}
