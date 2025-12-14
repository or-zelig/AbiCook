package il.co.or.abicook.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import il.co.or.abicook.R
import il.co.or.abicook.data.repository.RecipeRepositoryProvider

class MyRecipesFragment : Fragment() {

    private val repository = RecipeRepositoryProvider.recipeRepository
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_recipe_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvScreenTitle)
        val rvRecipes = view.findViewById<RecyclerView>(R.id.rvRecipes)
        val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)
        val btnCreateRecipe = view.findViewById<MaterialButton>(R.id.btnCreateRecipe)

        tvTitle.text = "My recipes"

        val adapter = RecipeFeedAdapter { recipe ->
            Toast.makeText(
                requireContext(),
                "Clicked: ${recipe.title}",
                Toast.LENGTH_SHORT
            ).show()
        }

        rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        rvRecipes.adapter = adapter

        repository.recipes.observe(viewLifecycleOwner, Observer { recipes ->
            val currentUser = auth.currentUser
            val filtered = if (currentUser == null) {
                emptyList()
            } else {
                recipes.filter { it.authorId == currentUser.uid }
            }

            adapter.submitList(filtered)
            tvEmptyState.text = "You have no recipes yet."
            tvEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        })

        btnCreateRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_myRecipesFragment_to_createRecipeFragment)
        }
    }
}
