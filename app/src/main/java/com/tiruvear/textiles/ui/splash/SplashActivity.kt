package com.tiruvear.textiles.ui.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tiruvear.textiles.R
import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.ui.auth.AuthActivity
import com.tiruvear.textiles.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.SessionStatus

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "SplashActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_splash)
            
            lifecycleScope.launch {
                try {
                    // Delay for at least 1 second to show splash screen
                    delay(1000)
                    
                    // Access supabase client safely
                    val supabase = TiruvearApp.supabaseClient
                    
                    // Check if user is logged in
                    when (supabase.gotrue.sessionStatus.value) {
                        is SessionStatus.Authenticated -> {
                            // User is logged in, navigate to main activity
                            Log.d(TAG, "User is authenticated, navigating to MainActivity")
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        }
                        else -> {
                            // User is not logged in, navigate to auth activity
                            Log.d(TAG, "User is not authenticated, navigating to AuthActivity")
                            startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                        }
                    }
                    
                    // Close splash activity
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in splash screen coroutine", e)
                    runOnUiThread {
                        Toast.makeText(this@SplashActivity, "Error starting app: ${e.message}", Toast.LENGTH_LONG).show()
                        // Still navigate to auth activity in case of error
                        startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                        finish()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in splash screen onCreate", e)
            Toast.makeText(this, "Failed to initialize app: ${e.message}", Toast.LENGTH_LONG).show()
            // Try to recover by starting auth activity
            try {
                startActivity(Intent(this, AuthActivity::class.java))
            } catch (e2: Exception) {
                Log.e(TAG, "Fatal error, unable to start AuthActivity", e2)
            }
            finish()
        }
    }
} 