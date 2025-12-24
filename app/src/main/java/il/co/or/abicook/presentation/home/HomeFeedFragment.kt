package il.co.or.abicook.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import il.co.or.abicook.R
import il.co.or.abicook.data.repository.FirestoreFeedRepository
import il.co.or.abicook.domain.repository.FeedSort

class HomeFeedFragment : Fragment() {

    private val viewModel: HomeFeedViewModel by viewModels {
        HomeFeedViewModelFactory(feedRepository = FirestoreFeedRepository())
    }

    private lateinit var adapter: RecipePostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Panels
        val panelFilters = view.findViewById<View>(R.id.panelFilters)
        val panelResults = view.findViewById<View>(R.id.panelResults)

        // Filters UI
        val tvGreetingFilters = view.findViewById<TextView>(R.id.tvGreetingFilters)
        val chipGroup = view.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupCategories)
        val rgSort = view.findViewById<RadioGroup>(R.id.rgSort)

        val switchTotalTime = view.findViewById<SwitchMaterial>(R.id.switchTotalTime)
        val timeRangeContainer = view.findViewById<View>(R.id.timeRangeContainer)
        val sliderTotalTime = view.findViewById<Slider>(R.id.sliderTotalTime)
        val tvTotalTimeValue = view.findViewById<TextView>(R.id.tvTotalTimeValue)

        val btnShow = view.findViewById<MaterialButton>(R.id.btnShowRecipes)

        // Results UI
        val tvGreetingResults = view.findViewById<TextView>(R.id.tvGreetingResults)
        val rvFeed = view.findViewById<RecyclerView>(R.id.rvFeed)
        val fabCreate = view.findViewById<FloatingActionButton>(R.id.fabCreate)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        val btnOpenFilters = view.findViewById<MaterialButton>(R.id.btnOpenFilters)

        // Greeting
        val user = FirebaseAuth.getInstance().currentUser
        val greeting = "Welcome, ${user?.email ?: "Cooker"}"
        tvGreetingFilters.text = greeting
        tvGreetingResults.text = greeting

        // Build category chips (same idea as אצלך):contentReference[oaicite:2]{index=2}
        val categories = resources.getStringArray(R.array.recipe_categories)
        chipGroup.removeAllViews()
        for (c in categories) {
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = c
                isCheckable = true
            }
            chipGroup.addView(chip)
        }

        // Toggle time UI
        timeRangeContainer.isVisible = switchTotalTime.isChecked
        tvTotalTimeValue.text = "${sliderTotalTime.value.toInt()} דקות"

        switchTotalTime.setOnCheckedChangeListener { _, checked ->
            timeRangeContainer.isVisible = checked
        }

        sliderTotalTime.addOnChangeListener { _, value, _ ->
            tvTotalTimeValue.text = "${value.toInt()} דקות"
        }

        // Recycler
        adapter = RecipePostAdapter()
        rvFeed.layoutManager = LinearLayoutManager(requireContext())
        rvFeed.adapter = adapter

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            state.error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
            adapter.submitList(state.posts)
        }

        // Default view = Filters
        panelFilters.isVisible = true
        panelResults.isVisible = false

        btnShow.setOnClickListener {
            val selectedCategories = (0 until chipGroup.childCount)
                .map { chipGroup.getChildAt(it) }
                .filterIsInstance<com.google.android.material.chip.Chip>()
                .filter { it.isChecked }
                .map { it.text.toString() }

            val sort = when (rgSort.checkedRadioButtonId) {
                R.id.rbMostLiked -> FeedSort.MOST_LIKED
                else -> FeedSort.NEWEST
            }

            // כרגע: רק UI של זמן (הצגה). אם תרצה שהזמן גם יסנן נתונים בפועל – נוסיף אחרי שזה יציב.
            viewModel.loadFeed(categories = selectedCategories, sort = sort)

            panelFilters.isVisible = false
            panelResults.isVisible = true
        }

        btnOpenFilters.setOnClickListener {
            panelResults.isVisible = false
            panelFilters.isVisible = true
        }

        fabCreate.setOnClickListener {
            findNavController().navigate(R.id.action_global_createRecipeFragment)

        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.loginFragment)
        }
    }
}
