package il.co.or.abicook.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import il.co.or.abicook.R
import il.co.or.abicook.data.repository.FirestoreFeedRepository

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

        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val rvFeed = view.findViewById<RecyclerView>(R.id.rvFeed)
        val fabCreate = view.findViewById<FloatingActionButton>(R.id.fabCreate)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)

        val user = FirebaseAuth.getInstance().currentUser
        tvGreeting.text = "Welcome, ${user?.email ?: "Cooker"}"

        adapter = RecipePostAdapter()
        rvFeed.layoutManager = LinearLayoutManager(requireContext())
        rvFeed.adapter = adapter

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.error != null) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
            }

            android.util.Log.d("FEED_UI", "render size=${state.posts.size}")
            adapter.submitList(state.posts)
        }

        viewModel.startObservingFeed()

        fabCreate.setOnClickListener {
            findNavController().navigate(R.id.action_homeFeedFragment_to_createRecipeFragment)
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.loginFragment)
        }
    }
}
