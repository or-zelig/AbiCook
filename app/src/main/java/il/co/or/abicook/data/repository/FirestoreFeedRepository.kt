package il.co.or.abicook.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import il.co.or.abicook.domain.model.RecipePost
import il.co.or.abicook.domain.repository.FeedRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreFeedRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : FeedRepository {

    override fun observeHomeFeed(): Flow<List<RecipePost>> = callbackFlow {
        val currentUid = auth.currentUser?.uid

        var registration: ListenerRegistration? = null

        registration = firestore.collection("recipes")
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FEED", "snapshot error", error)
                    close(error)
                    return@addSnapshotListener
                }

                android.util.Log.d("FEED", "docs=${snapshot?.documents?.size ?: 0}")

                val posts = snapshot?.documents.orEmpty().map { doc ->
                    android.util.Log.d("FEED", "docId=${doc.id} data=${doc.data}")

                    // זמני: רק כותרת + תיאור כדי לוודא שממפה
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
                        isLikedByMe = false,
                        category = doc.getString("primaryCategory").orEmpty(),
                        prepTime = (doc.getLong("prepTimeMin") ?: 0L).toInt(),
                        cookTime = (doc.getLong("cookTimeMin") ?: 0L).toInt()
                    )
                }

                trySend(posts)
            }


        /* registration = firestore.collection("recipes")
        .orderBy("createdAtMillis", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                // נסגור את ה-Flow עם שגיאה (ה-VM יציג)
                close(error)
                return@addSnapshotListener
            }

            val docs = snapshot?.documents.orEmpty()

            val posts = docs.map { doc ->
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
                    isLikedByMe = false,

                    category = doc.getString("primaryCategory").orEmpty(),
                    prepTime = (doc.getLong("prepTimeMin") ?: 0L).toInt(),
                    cookTime = (doc.getLong("cookTimeMin") ?: 0L).toInt()
                )
            }

            trySend(posts)
        } */

        awaitClose { registration?.remove() }
    }
}
