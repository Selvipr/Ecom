package com.tiruvear.textiles.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.tiruvear.textiles.R
import com.tiruvear.textiles.databinding.ActivityMainBinding
import com.tiruvear.textiles.utils.DatabaseTester

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var currentNavId: Int = R.id.navigation_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        try {
            setupNavigation()
        } catch (e: Exception) {
            Log.e(TAG, "Navigation setup failed: ${e.message}", e)
            Toast.makeText(this, "Navigation setup failed: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Fallback to show at least the home fragment if navigation fails
            try {
                showHomeFragment()
            } catch (e: Exception) {
                Log.e(TAG, "Even fallback failed: ${e.message}", e)
            }
        }
        
        // Set up bottom navigation click listeners with error handling
        setupBottomNavWithErrorHandling()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_test_database -> {
                // Test database connection
                DatabaseTester.testDatabaseConnection(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showHomeFragment() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navHostFragment?.let {
            val inflater = it.navController.navInflater
            val graph = inflater.inflate(R.navigation.main_navigation)
            graph.setStartDestination(R.id.navigation_home)
            it.navController.graph = graph
        }
    }
    
    private fun setupBottomNavWithErrorHandling() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            try {
                when (item.itemId) {
                    R.id.navigation_home, 
                    R.id.navigation_categories,
                    R.id.navigation_cart,
                    R.id.navigation_orders,
                    R.id.navigation_profile -> {
                        // Only navigate if we're changing destinations
                        if (currentNavId != item.itemId) {
                            currentNavId = item.itemId
                            navController.navigate(item.itemId)
                        }
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Navigation failed: ${e.message}", e)
                Toast.makeText(this, "Navigation error. Please try again.", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Set up bottom navigation with the nav controller
        try {
            binding.bottomNavigation.setupWithNavController(navController)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up bottom navigation: ${e.message}", e)
        }
        
        // Set up the action bar with the nav controller
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_categories,
                R.id.navigation_cart,
                R.id.navigation_orders,
                R.id.navigation_profile
            )
        )
        
        try {
            setupActionBarWithNavController(navController, appBarConfiguration)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up action bar: ${e.message}", e)
        }
        
        // Listen for navigation changes to update the toolbar title
        navController.addOnDestinationChangedListener { _, destination, _ ->
            try {
                updateToolbarTitle(destination)
                currentNavId = destination.id
            } catch (e: Exception) {
                Log.e(TAG, "Error updating toolbar title: ${e.message}", e)
            }
        }
    }
    
    private fun updateToolbarTitle(destination: NavDestination) {
        binding.toolbar.title = try {
            destination.label ?: getString(R.string.app_name)
        } catch (e: Exception) {
            getString(R.string.app_name)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return try {
            navController.navigateUp() || super.onSupportNavigateUp()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating up: ${e.message}", e)
            super.onSupportNavigateUp()
        }
    }
} 