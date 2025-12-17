package il.co.or.abicook.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import il.co.or.abicook.presentation.home.RecipeUiItem
import il.co.or.abicook.presentation.home.RecipesRepository
import kotlinx.coroutines.tasks.await

class FirestoreRecipesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RecipesRepository {

    override suspend fun getAllRecipes(): List<RecipeUiItem> {
        val snap = firestore.collection("recipes")
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .get()
            .await()

        return snap.documents.map { doc ->
            RecipeUiItem(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                category = doc.getString("primaryCategory").orEmpty(),
                likes = (doc.getLong("likes") ?: 0L).toInt(),
                prepTime = (doc.getLong("prepTimeMin") ?: 0L).toInt(),
                cookTime = (doc.getLong("cookTimeMin") ?: 0L).toInt(),
                createdAtMillis = doc.getLong("createdAtMillis") ?: 0L
            )
        }
    }
}
