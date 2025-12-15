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

class MyRecipesFragment : Fragment(R.layout.fragment_placeholder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvScreenTitle).text = "My Recipes"
        view.findViewById<TextView>(R.id.tvEmptyState).text = "Soon you'll see your recipes here"
        view.findViewById<MaterialButton>(R.id.btnCreateRecipe).text = "Create Recipe"
    }
}

