package il.co.or.abicook.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import il.co.or.abicook.domain.model.RecipePost
import kotlinx.coroutines.tasks.await

class FirestoreRecipeDataRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val recipesCol = db.collection("recipes")

    suspend fun loadFeed(limit: Long = 50): List<RecipePost> {
        val snap = recipesCol
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        val myUid = auth.currentUser?.uid.orEmpty()

        return snap.documents.map { doc ->
            RecipePost(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                description = doc.getString("description").orEmpty(),
                imageUrl = doc.getString("imageUrl"),

                authorId = doc.getString("authorId").orEmpty(),
                authorName = doc.getString("authorName").orEmpty(),

                createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,
                likes = doc.getLong("likes") ?: 0L,
                commentsCount = doc.getLong("commentsCount") ?: 0L,

                // כרגע אין לך likes-by-user, אז נשאיר false
                isLikedByMe = false,

                ingredientsSummary = doc.getString("ingredientsSummary").orEmpty(),
                stepsSummary = doc.getString("stepsSummary").orEmpty(),

                primaryCategory = doc.getString("primaryCategory").orEmpty(),
                categories = (doc.get("categories") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                prepTimeMin = (doc.getLong("prepTimeMin") ?: 0L).toInt(),
                cookTimeMin = (doc.getLong("cookTimeMin") ?: 0L).toInt()
            )
        }
    }

    suspend fun createRecipe(post: RecipePost): String {
        val uid = auth.currentUser?.uid ?: error("Not logged in")

        val data = hashMapOf(
            "title" to post.title,
            "description" to post.description,
            "imageUrl" to post.imageUrl,

            "authorId" to uid,
            "authorName" to post.authorName, // אם אין לך שם עדיין – נשים "Anonymous" מה-VM
            "createdAtMillis" to System.currentTimeMillis(),

            "likes" to 0L,
            "commentsCount" to 0L,

            "ingredientsSummary" to post.ingredientsSummary,
            "stepsSummary" to post.stepsSummary,

            "primaryCategory" to post.primaryCategory,
            "categories" to post.categories,
            "prepTimeMin" to post.prepTimeMin,
            "cookTimeMin" to post.cookTimeMin
        )

        val ref = recipesCol.add(data).await()
        return ref.id
    }
}
