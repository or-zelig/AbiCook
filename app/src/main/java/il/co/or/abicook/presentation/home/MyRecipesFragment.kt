package il.co.or.abicook.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import il.co.or.abicook.R
import il.co.or.abicook.domain.repository.FeedSort
import kotlinx.coroutines.launch

class MyRecipesFragment : Fragment(R.layout.fragment_my_recipes) {

    private val vm: MyRecipesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val panelFilters = view.findViewById<View>(R.id.panelFilters)
        val panelResults = view.findViewById<View>(R.id.panelResults)

        val tvUser = view.findViewById<TextView>(R.id.tvUser)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupCategories)
        val rgSort = view.findViewById<RadioGroup>(R.id.rgSort)
        val btnShow = view.findViewById<MaterialButton>(R.id.btnShow)

        val btnEditFilters = view.findViewById<MaterialButton>(R.id.btnEditFilters)
        val rv = view.findViewById<RecyclerView>(R.id.rvMyRecipes)
        val progress = view.findViewById<ProgressBar>(R.id.progress)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        // Header username
        val user = FirebaseAuth.getInstance().currentUser
        tvUser.text = "My recipes: ${user?.email ?: "Unknown"}"

        // Build category chips
        val categories = resources.getStringArray(R.array.recipe_categories)
        chipGroup.removeAllViews()
        categories.forEach { c ->
            chipGroup.addView(
                Chip(requireContext()).apply {
                    text = c
                    isCheckable = true
                }
            )
        }

        // Recycler
        val adapter = RecipePostAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Observe state
        viewLifecycleOwner.lifecycleScope.launch {
            vm.uiState.collect { s ->
                progress.isVisible = s.isLoading
                tvEmpty.isVisible = !s.isLoading && s.error == null && s.recipes.isEmpty()
                adapter.submitList(s.recipes)
            }
        }

        btnEditFilters.setOnClickListener {
            panelResults.isVisible = false
            panelFilters.isVisible = true
        }

        btnShow.setOnClickListener {
            val selectedCategories = (0 until chipGroup.childCount)
                .map { chipGroup.getChildAt(it) }
                .filterIsInstance<Chip>()
                .filter { it.isChecked }
                .map { it.text.toString() }

            val sortOption = when (rgSort.checkedRadioButtonId) {
                R.id.rbMostLiked -> FeedSort.MOST_LIKED
                else -> FeedSort.NEWEST
            }

            vm.loadMyRecipes(categories = selectedCategories, sort = sortOption)

            panelFilters.isVisible = false
            panelResults.isVisible = true
        }
    }
}
