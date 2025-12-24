package il.co.or.abicook.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedSort
import kotlinx.coroutines.tasks.await

class FirestoreMyRecipesRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getMyRecipes(
        userId: String,
        categories: List<String>,
        sort: FeedSort
    ): List<RecipePost> {

        // ✅ אצלך זה authorId (לא userId)
        var q: Query = db.collection("recipes")
            .whereEqualTo("authorId", userId)

        if (categories.isNotEmpty()) {
            q = q.whereArrayContainsAny("categories", categories)
        }

        q = when (sort) {
            FeedSort.MOST_LIKED -> q.orderBy("likes", Query.Direction.DESCENDING)
            FeedSort.NEWEST -> q.orderBy("createdAtMillis", Query.Direction.DESCENDING)
        }

        // ✅ במקום FieldPath: משתמשים בשדה המערכת "__name__"
        q = q.orderBy("__name__", Query.Direction.DESCENDING)

        val snap = q.get().await()

        // אל תשתמש ב-toObject על Kotlin data class אם אין no-arg ctor → מפה ידנית
        return snap.documents.map { doc ->
            RecipePost(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                description = doc.getString("description").orEmpty(),
                imageUrl = doc.getString("imageUrl"),

                authorId = doc.getString("authorId").orEmpty(),
                authorName = doc.getString("authorName") ?: "Unknown",

                createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,

                likes = doc.getLong("likes") ?: 0L,
                commentsCount = doc.getLong("commentsCount") ?: 0L,
                isLikedByMe = false,

                ingredientsSummary = doc.getString("ingredientsSummary").orEmpty(),
                stepsSummary = doc.getString("stepsSummary").orEmpty(),

                primaryCategory = doc.getString("primaryCategory").orEmpty(),
                categories = (doc.get("categories") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                prepTimeMin = (doc.getLong("prepTimeMin") ?: 0L).toInt(),
                cookTimeMin = (doc.getLong("cookTimeMin") ?: 0L).toInt()
            )
        }
    }
}
