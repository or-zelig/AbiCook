package il.co.or.abicook

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import il.co.or.abicook.presentation.home.FeedFilterViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    // כדי שנוכל לעשות clear() בפילטר ב-Logout
    private val filterVm: FeedFilterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val fab = findViewById<FloatingActionButton>(R.id.fabCreateRecipe)

        // + מכל מקום -> CreateRecipe
        fab.setOnClickListener {
            if (navController.currentDestination?.id != R.id.createRecipeFragment) {
                navController.navigate(R.id.action_global_createRecipeFragment)
            }
        }

        // Bottom nav navigation + Logout handler
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_logout -> {
                    showLogoutDialog()
                    false // לא מסמנים את הטאב כפעיל, כי זה לא דף
                }

                else -> {
                    // ניווט רגיל בין דפים (Feed/My area)
                    val options = navOptions {
                        // לא popUpTo(startDestinationId) כי אצלך זה loginFragment
                        popUpTo(R.id.homeFeedFragment) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }

                    try {
                        navController.navigate(item.itemId, null, options)
                        true
                    } catch (e: IllegalArgumentException) {
                        false
                    }
                }
            }
        }

        // להציג/להסתיר BottomNav+FAB לפי מסך
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showChrome = destination.id == R.id.homeFeedFragment ||
                    destination.id == R.id.myAreaFragment ||
                    destination.id == R.id.createRecipeFragment

            bottomNav.isVisible = showChrome
            fab.isVisible = showChrome

            // סימון טאב פעיל רק אם הוא אחד מהדפים
            if (destination.id == R.id.homeFeedFragment || destination.id == R.id.myAreaFragment) {
                bottomNav.menu.findItem(destination.id)?.isChecked = true
            }
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Logout") { _, _ ->
                // ניקוי פילטרים
                filterVm.clear()

                // יציאה מהמשתמש
                FirebaseAuth.getInstance().signOut()

                // מעבר ל-Login וניקוי back stack
                navController.navigate(
                    R.id.loginFragment,
                    null,
                    navOptions {
                        popUpTo(R.id.nav_graph) { inclusive = true }
                        launchSingleTop = true
                    }
                )
            }
            .show()
    }
}
