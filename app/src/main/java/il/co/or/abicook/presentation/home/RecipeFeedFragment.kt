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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import il.co.or.abicook.R
import il.co.or.abicook.data.model.Recipe
import il.co.or.abicook.data.repository.RecipeRepositoryProvider

class RecipeFeedFragment : Fragment() {

    private val repository = RecipeRepositoryProvider.recipeRepository

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

        tvTitle.text = "Feed"

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
            adapter.submitList(recipes)
            tvEmptyState.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
        })

        btnCreateRecipe.setOnClickListener {
            findNavController().navigate(R.id.action_recipeFeedFragment_to_createRecipeFragment)
        }
    }
}

/* Adapter משותף גם למסך MyRecipes */
private class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
    override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean =
        oldItem == newItem
}

class RecipeFeedAdapter(
    private val onItemClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeFeedAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvRecipeDescription)

        fun bind(recipe: Recipe) {
            tvTitle.text = recipe.title
            tvDescription.text = recipe.description

            itemView.setOnClickListener {
                onItemClick(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
