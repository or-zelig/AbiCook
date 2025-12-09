package il.co.or.abicook

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavView = findViewById(R.id.bottomNavView)
        val fabCreateRecipe = findViewById<FloatingActionButton>(R.id.fabCreateRecipe)

        // קישור אוטומטי בין bottom nav ל־nav graph
        NavigationUI.setupWithNavController(bottomNavView, navController)

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes") { _, _ ->
                            FirebaseAuth.getInstance().signOut()
                            // חזרה למסך החיבור
                            navController.navigate(R.id.loginFragment)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideOn = setOf(
                R.id.loginFragment,
                R.id.createRecipeFragment
            )

            if (hideOn.contains(destination.id)) {
                bottomNavView.visibility = View.GONE
                fabCreateRecipe.visibility = View.GONE
            } else {
                bottomNavView.visibility = View.VISIBLE
                fabCreateRecipe.visibility = View.VISIBLE
            }
        }


        fabCreateRecipe.setOnClickListener {
            navController.navigate(R.id.action_global_createRecipeFragment)
        }

    }
}
