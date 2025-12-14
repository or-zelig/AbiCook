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
                if (error != null) {
                    // אפשר לעשות לוג, אבל לא נקרוס
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    val recipe = doc.toObject(Recipe::class.java)
                    recipe?.copy(id = doc.id)
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
            "createdAtMillis" to recipe.createdAtMillis,
            "authorId" to recipe.authorId
        )

        recipesCollection
            .add(data)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    override fun getRecipe(id: String): Recipe? =
        _recipes.value.orEmpty().firstOrNull { it.id == id }
}

