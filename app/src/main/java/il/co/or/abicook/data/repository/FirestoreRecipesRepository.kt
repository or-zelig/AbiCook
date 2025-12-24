package il.co.or.abicook.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import il.co.or.abicook.presentation.home.RecipeUiItem
import il.co.or.abicook.presentation.home.RecipesRepository
import kotlinx.coroutines.tasks.await

class FirestoreRecipesRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RecipesRepository {

    override suspend fun getAllRecipes(): List<RecipeUiItem> {
        val snapshot = db.collection("recipes")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            val title = doc.getString("title").orEmpty()
            val description = doc.getString("description").orEmpty()

            val createdAtMillis = doc.getLong("createdAtMillis") ?: 0L
            val authorId = doc.getString("authorId")

            val ingredientsSummary = doc.getString("ingredientsSummary")
            val stepsSummary = doc.getString("stepsSummary")

            // אם בעתיד תוסיף תמונה לפוסט:
            val imageUrl = doc.getString("imageUrl")

            // אם בעתיד תוסיף likes:
            val likes = (doc.getLong("likes") ?: 0L).toInt()

            // אם בעתיד תוסיף זמנים:
            val prep = (doc.getLong("prepTimeMinutes") ?: 0L).toInt()
            val cook = (doc.getLong("cookTimeMinutes") ?: 0L).toInt()
            val totalTime = if (prep + cook > 0) prep + cook else 0

            // אם בעתיד תוסיף categories כ־array:
            val categories = (doc.get("categories") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

            RecipeUiItem(
                id = doc.id,
                title = title,
                description = description,
                imageUrl = imageUrl,
                createdAtMillis = createdAtMillis,
                likes = likes,
                totalTimeMinutes = totalTime,
                categories = categories,
                authorId = authorId,
                ingredientsSummary = ingredientsSummary,
                stepsSummary = stepsSummary
            )
        }
    }
}
