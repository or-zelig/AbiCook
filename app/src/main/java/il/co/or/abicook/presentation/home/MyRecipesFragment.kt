package il.co.or.abicook.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import il.co.or.abicook.R

class MyRecipesFragment : Fragment(R.layout.fragment_placeholder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvScreenTitle).text = "My Recipes"
        view.findViewById<TextView>(R.id.tvEmptyState).text = "Soon you'll see your recipes here"
        view.findViewById<MaterialButton>(R.id.btnCreateRecipe).text = "Create Recipe"
    }
}

