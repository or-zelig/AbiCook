package il.co.or.abicook.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import il.co.or.abicook.data.model.Recipe

class FirestoreRecipeRepository : RecipeRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val recipesCollection = firestore.collection("recipes")

    private val _recipes = MutableLiveData<List<Recipe>>(emptyList())
    override val recipes: LiveData<List<Recipe>> = _recipes

    init {
        recipesCollection
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val list = snapshot?.documents?.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: ""
                    Recipe(
                        id = doc.id,
                        title = title,
                        description = description,
                        ingredientsSummary = doc.getString("ingredientsSummary") ?: "",
                        stepsSummary = doc.getString("stepsSummary") ?: "",
                        createdAtMillis = doc.getLong("createdAtMillis") ?: 0L,
                        authorId = doc.getString("authorId") ?: "",
                        imageUrl = doc.getString("imageUrl")
                    )
                } ?: emptyList()

                _recipes.value = list
            }
    }

    override fun addRecipe(
        recipe: Recipe,
        onResult: (Boolean, String?) -> Unit
    ) {
        val data = hashMapOf(
            "title" to recipe.title,
            "description" to recipe.description,
            "ingredientsSummary" to recipe.ingredientsSummary,
            "stepsSummary" to recipe.stepsSummary,

            "primaryCategory" to recipe.primaryCategory,
            "categories" to recipe.categories,
            "prepTimeMin" to recipe.prepTimeMin,
            "cookTimeMin" to recipe.cookTimeMin,

            "imageUrl" to recipe.imageUrl,

            "createdAtMillis" to recipe.createdAtMillis,
            "authorId" to recipe.authorId,
            "authorName" to recipe.authorName,

            "likes" to recipe.likes,
            "commentsCount" to recipe.commentsCount
        )

        recipesCollection
            .add(data)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }


    override fun getRecipe(id: String): Recipe? =
        _recipes.value.orEmpty().firstOrNull { it.id == id }
}
