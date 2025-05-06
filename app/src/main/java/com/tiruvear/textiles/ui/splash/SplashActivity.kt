package com.tiruvear.textiles.ui.splash

import android.content.Intent
import android.os.Bundle
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
    
    private val supabase = TiruvearApp.supabaseClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        lifecycleScope.launch {
            // Delay for at least 1 second to show splash screen
            delay(1000)
            
            // Check if user is logged in
            when (supabase.gotrue.sessionStatus.value) {
                is SessionStatus.Authenticated -> {
                    // User is logged in, navigate to main activity
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }
                else -> {
                    // User is not logged in, navigate to auth activity
                    startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                }
            }
            
            // Close splash activity
            finish()
        }
    }
} 