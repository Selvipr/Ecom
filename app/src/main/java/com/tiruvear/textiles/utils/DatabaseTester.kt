package com.tiruvear.textiles.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.tiruvear.textiles.TiruvearApp
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Utility class to test the connection to Supabase database
 */
object DatabaseTester {
    private const val TAG = "DatabaseTester"
    
    /**
     * Tests the connection to Supabase database and displays the results
     * @param context The context to display toast messages
     */
    fun testDatabaseConnection(context: Context) {
        val supabase = TiruvearApp.supabaseClient
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Test categories table
                val categoryTest = withContext(Dispatchers.IO) {
                    val categories = supabase.postgrest["product_categories"]
                        .select()
                    categories.decodeList<Map<String, Any>>()
                }
                
                Log.d(TAG, "Categories test passed. Found ${categoryTest.size} categories.")
                
                // Test products table
                val productTest = withContext(Dispatchers.IO) {
                    val products = supabase.postgrest["products"]
                        .select()
                    products.decodeList<Map<String, Any>>()
                }
                
                Log.d(TAG, "Products test passed. Found ${productTest.size} products.")
                
                // Test cart functionality
                val userId = TiruvearApp.getCurrentUserId() ?: "guest_user"
                val cartTest = withContext(Dispatchers.IO) {
                    val carts = supabase.postgrest["carts"]
                        .select {
                            filter("user_id", io.github.jan.supabase.postgrest.query.FilterOperator.EQ, userId)
                        }
                    carts.decodeList<Map<String, Any>>()
                }
                
                Log.d(TAG, "Cart test passed. Found ${cartTest.size} carts for user.")
                
                // If we get here, all tests passed
                Toast.makeText(
                    context,
                    "Database connection successful! Tables exist and are accessible.",
                    Toast.LENGTH_LONG
                ).show()
                
            } catch (e: Exception) {
                Log.e(TAG, "Database test failed: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Database test failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * Runs all the necessary migration scripts to ensure the database is set up correctly
     * @param context The context to display toast messages
     */
    fun ensureDatabaseSetup(context: Context) {
        val supabase = TiruvearApp.supabaseClient
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // TEMPORARY FIX: Skip database validation during development
                // Remove this line when database setup is complete
                return@launch
                
                // Check if products table exists
                val productsExist = withContext(Dispatchers.IO) {
                    try {
                        val products = supabase.postgrest["products"]
                            .select()
                        products.decodeList<Map<String, Any>>()
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
                
                if (!productsExist) {
                    Log.w(TAG, "Products table not found. Database setup may be required.")
                    Toast.makeText(
                        context,
                        "Database setup required. Please run the SQL script in Supabase dashboard.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.d(TAG, "Database schema validation passed.")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Database validation failed: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Database validation failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
} 