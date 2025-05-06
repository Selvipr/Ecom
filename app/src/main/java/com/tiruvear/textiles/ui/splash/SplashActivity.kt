package com.tiruvear.textiles.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.ui.auth.AuthActivity
import com.tiruvear.textiles.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // No need to set content view as we're using a theme with background
        
        lifecycleScope.launch {
            // Delay for at least 1 second to show splash screen
            delay(1000)
            
            // Check if user is already logged in
            val session = TiruvearApp.supabaseClient.auth.currentSession
            
            if (session != null) {
                // User is logged in, navigate to main activity
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                // User is not logged in, navigate to auth activity
                startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
            }
            
            // Close splash activity
            finish()
        }
    }
} 