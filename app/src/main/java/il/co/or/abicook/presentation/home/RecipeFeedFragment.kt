package il.co.or.abicook.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import il.co.or.abicook.R
import il.co.or.abicook.databinding.FragmentRecipeFeedBinding

class RecipeFeedFragment : Fragment(R.layout.fragment_recipe_feed) {

    private var _b: FragmentRecipeFeedBinding? = null
    private val b get() = _b!!

    // TODO: חבר ל-Repository האמיתי שלך (Firestore) — בינתיים Stub
    private val repo: RecipesRepository = object : RecipesRepository {
        override suspend fun getAllRecipes(): List<RecipeUiItem> {
            return emptyList() // כרגע. כשנחבר Firestore, זה יתמלא.
        }
    }

    private val vm: RecipeFeedViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RecipeFeedViewModel(repo) as T
            }
        })[RecipeFeedViewModel::class.java
        ]
    }

    private val adapter = RecipeFeedAdapter { item ->
        // TODO: בשלב הבא: ניווט למסך RecipeDetails
        Toast.makeText(requireContext(), "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentRecipeFeedBinding.bind(view)

        setupRecycler()
        setupCategoryChips()

        b.switchLimitTime.setOnCheckedChangeListener { _, checked ->
            b.tilMaxTime.isVisible = checked
            if (!checked) b.etMaxTime.setText("")
        }

        b.btnShowRecipes.setOnClickListener {
            val selectedCategories = getSelectedCategories()
            if (selectedCategories.isEmpty()) {
                Toast.makeText(requireContext(), "בחר לפחות קטגוריה אחת", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sortMode = if (b.rbMostLiked.isChecked) SortMode.MOST_LIKED else SortMode.NEWEST
            val maxTime = if (b.switchLimitTime.isChecked) {
                val s = b.etMaxTime.text?.toString()?.trim().orEmpty()
                if (s.isBlank()) {
                    Toast.makeText(requireContext(), "מלא זמן מקסימלי", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                s.toIntOrNull()?.also {
                    if (it <= 0) {
                        Toast.makeText(requireContext(), "הכנס מספר דקות תקין", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                } ?: run {
                    Toast.makeText(requireContext(), "הכנס מספר דקות תקין", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else null

            vm.applyFilters(
                FeedFilters(
                    categories = selectedCategories,
                    sortMode = sortMode,
                    maxTotalTimeMinutes = maxTime
                )
            )
        }

        b.btnEditFilters.setOnClickListener {
            vm.backToFilters()
        }

        vm.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }

        // טוען data פעם אחת
        vm.loadAllIfNeeded()
    }

    private fun setupRecycler() {
        b.rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        b.rvRecipes.adapter = adapter
    }

    private fun setupCategoryChips() {
        b.chipGroupCategories.removeAllViews()

        val categories = resources.getStringArray(R.array.recipe_categories).toList()

        // ברירת מחדל: הכל מסומן (כדי שהמשתמש לא יראה ריק)
        categories.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text = cat
                isCheckable = true
                isChecked = true
            }
            b.chipGroupCategories.addView(chip)
        }
    }

    private fun getSelectedCategories(): Set<String> {
        val set = mutableSetOf<String>()
        for (i in 0 until b.chipGroupCategories.childCount) {
            val chip = b.chipGroupCategories.getChildAt(i) as? Chip ?: continue
            if (chip.isChecked) set.add(chip.text.toString())
        }
        return set
    }

    private fun render(state: FeedUiState) {
        when (state) {
            is FeedUiState.Idle -> {
                b.panelFilters.isVisible = true
                b.panelResults.isVisible = false
                b.progress.isVisible = false
            }

            is FeedUiState.Loading -> {
                // עדיין במסך פילטרים אבל עם עיגול (טעינת מתכונים)
                b.panelFilters.isVisible = true
                b.panelResults.isVisible = false
            }

            is FeedUiState.Results -> {
                b.panelFilters.isVisible = false
                b.panelResults.isVisible = true
                b.progress.isVisible = false

                adapter.submitList(state.items)
                b.tvEmpty.isVisible = state.items.isEmpty()
                b.rvRecipes.isVisible = state.items.isNotEmpty()
            }

            is FeedUiState.Error -> {
                b.panelFilters.isVisible = true
                b.panelResults.isVisible = false
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
