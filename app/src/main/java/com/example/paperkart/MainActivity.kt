package com.example.paperkart

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.databinding.ActivityMainBinding
import com.example.paperkart.features.auth.AuthActivity
import com.example.paperkart.features.categories.CategoriesFragment
import com.example.paperkart.features.home.HomeFragment
import com.example.paperkart.features.user.ProfileFragment

// Import your other fragments here as you create them:
// import com.example.paperkart.features.categories.CategoriesFragment
// import com.example.paperkart.features.orders.OrdersFragment
// import com.example.paperkart.features.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Session Check: Redirect to Login if no token exists
        val session = SessionManager(this)
        if (session.getToken() == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        // 2. Production UI: Edge-to-Edge with Dark System Icons
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. System Bar Insets: Prevents content from hiding under status/nav bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding to root to respect system bars
            v.updatePadding(top = insets.top, bottom = insets.bottom)
            windowInsets
        }

        // 4. Initialize Default Fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // 5. Setup Bottom Navigation Click Logic
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_categories -> {
//                     Replace with CategoriesFragment() once created
                     loadFragment(CategoriesFragment())
                    true
                }
                R.id.nav_orders -> {
                    // Replace with OrdersFragment() once created
                    // loadFragment(OrdersFragment())
                    true
                }
                R.id.nav_profile -> {
                    // Replace with ProfileFragment() once created
                     loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Helper function to swap fragments in the fragmentContainer
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .setReorderingAllowed(true)
            .commit()
    }
}