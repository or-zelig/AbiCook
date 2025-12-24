package il.co.or.abicook.data.repository

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import il.co.or.abicook.data.mapper.RecipeMapper.toPost
import il.co.or.abicook.data.model.Recipe
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedSort
import kotlinx.coroutines.tasks.await

class FirestoreMyRecipesRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getMyRecipes(
        userId: String,
        categories: List<String>,
        sort: FeedSort
    ): List<RecipePost> {

        // IMPORTANT: אצלך זה authorId (לא userId)
        var q: Query = firestore.collection("recipes")
            .whereEqualTo("authorId", userId)

        // Filter by categories (optional)
        if (categories.isNotEmpty()) {
            q = q.whereArrayContainsAny("categories", categories)
        }

        q = when (sort) {
            FeedSort.NEWEST -> q.orderBy("createdAtMillis", Query.Direction.DESCENDING)
            FeedSort.MOST_LIKED -> q.orderBy("likes", Query.Direction.DESCENDING)
        }

        // stable tie-breaker (חובה import ל-FieldPath)
        q = q.orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)

        val snap = q.limit(50).get().await()

        val recipes = snap.documents.mapNotNull { doc ->
            doc.toObject(Recipe::class.java)?.copy(id = doc.id)
        }

        return recipes.map { it.toPost() }
    }
}
