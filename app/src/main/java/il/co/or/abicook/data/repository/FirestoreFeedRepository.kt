package il.co.or.abicook.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedRepository
import kotlinx.coroutines.tasks.await

class FirestoreFeedRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : FeedRepository {

    override suspend fun getHomeFeed(): List<RecipePost> {
        val currentUid = auth.currentUser?.uid

        val snapshot = firestore.collection("recipes")
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            RecipePost(
                id = doc.id,
                title = doc.getString("title").orEmpty(),

                description = doc.getString("description").orEmpty(),
                ingredientsSummary = doc.getString("ingredientsSummary").orEmpty(),
                stepsSummary = doc.getString("stepsSummary").orEmpty(),
                createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,
                authorId = doc.getString("authorId").orEmpty(),

                imageUrl = doc.getString("imageUrl"),
                authorName = doc.getString("authorName") ?: "Unknown",
                likes = (doc.getLong("likes") ?: 0L).toInt(),
                commentsCount = (doc.getLong("commentsCount") ?: 0L).toInt(),
                isLikedByMe = false, // כרגע אין לנו likes-per-user

                // Feed V2 פילטור (אם עוד לא שמרת במסד — פשוט יהיה default)
                category = doc.getString("primaryCategory").orEmpty(),
                prepTime = (doc.getLong("prepTimeMin") ?: 0L).toInt(),
                cookTime = (doc.getLong("cookTimeMin") ?: 0L).toInt()
            )
        }
    }
}
