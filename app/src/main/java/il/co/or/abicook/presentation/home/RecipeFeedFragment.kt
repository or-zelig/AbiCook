package il.co.or.abicook.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import il.co.or.abicook.R
import il.co.or.abicook.data.repository.RecipeFeedViewModel
import kotlinx.coroutines.launch

class RecipeFeedFragment : Fragment(R.layout.fragment_recipe_feed) {

    private val vm: RecipeFeedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvRecipes)
        val progress = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyState)
        val tvError = view.findViewById<TextView>(R.id.tvError)

        val adapter = RecipePostAdapter()
        rv.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    progress.isVisible = state.isLoading

                    tvError.isVisible = state.error != null
                    tvError.text = state.error.orEmpty()

                    val isEmpty = !state.isLoading && state.error == null && state.recipes.isEmpty()
                    tvEmpty.isVisible = isEmpty

                    adapter.submitList(state.recipes)
                }
            }
        }

        vm.load()
    }
}
