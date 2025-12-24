package il.co.or.abicook.data.repository.firestore

import com.google.firebase.firestore.FirebaseFirestore
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.RecipesRepository
import kotlinx.coroutines.tasks.await

class FirestoreRecipeDataRepository(
    private val firestore: FirebaseFirestore
) : RecipesRepository {

    override suspend fun getFeedRecipes(): List<RecipePost> {
        return firestore.collection("recipes")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(RecipePost::class.java) }
    }

    override suspend fun getMyRecipes(userId: String): List<RecipePost> {
        return firestore.collection("recipes")
            .whereEqualTo("authorId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(RecipePost::class.java) }
    }

    override suspend fun createRecipe(recipe: RecipePost) {
        firestore.collection("recipes")
            .add(recipe)
            .await()
    }
}
