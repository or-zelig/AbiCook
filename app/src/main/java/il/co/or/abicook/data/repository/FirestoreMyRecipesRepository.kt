package il.co.or.abicook.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedSort
import kotlinx.coroutines.tasks.await

class FirestoreMyRecipesRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getMyRecipes(
        userId: String,
        categories: List<String>,
        sort: FeedSort,
    ): List<RecipePost> {

        // 1) רק לפי authorId => לא דורש קומפוזיט אינדקס
        val snap = db.collection("recipes")
            .whereEqualTo("authorId", userId)
            .get()
            .await()

        Log.d("MY_RECIPES", "uid=$userId docs=${snap.size()}")

        // 2) מיפוי למסך
        val posts = snap.documents.map { doc ->
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

        // 3) פילטור קטגוריות בצד לקוח (כדי לא לדרוש אינדקסים)
        val filtered = if (categories.isEmpty()) {
            posts
        } else {
            val wanted = categories.toSet()
            posts.filter { p ->
                p.primaryCategory in wanted || p.categories.any { it in wanted }
            }
        }

        // 4) מיון בצד לקוח
        return when (sort) {
            FeedSort.NEWEST ->
                filtered.sortedWith(compareByDescending<RecipePost> { it.createdAtMillis }
                    .thenByDescending { it.id })

            FeedSort.MOST_LIKED ->
                filtered.sortedWith(compareByDescending<RecipePost> { it.likes }
                    .thenByDescending { it.createdAtMillis }
                    .thenByDescending { it.id })
        }
    }
}
