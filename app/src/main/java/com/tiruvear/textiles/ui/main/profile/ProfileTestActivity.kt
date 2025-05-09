package com.tiruvear.textiles.ui.main.profile

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.util.SessionManager

/**
 * Simple activity to test that profile changes persist
 * This can be used for debugging the profile update issue
 */
class ProfileTestActivity : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var tvUserInfo: TextView
    private lateinit var btnRefresh: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_test)
        
        sessionManager = SessionManager(this)
        
        tvUserInfo = findViewById(R.id.tv_user_info)
        btnRefresh = findViewById(R.id.btn_refresh)
        
        btnRefresh.setOnClickListener {
            displayUserInfo()
        }
        
        displayUserInfo()
    }
    
    private fun displayUserInfo() {
        val userData = sessionManager.getUserData()
        
        if (userData != null) {
            val userInfo = """
                User ID: ${userData.id}
                Name: ${userData.name}
                Email: ${userData.email}
                Phone: ${userData.phone ?: "Not set"}
                Profile Image: ${userData.profileImageUrl ?: "Not set"}
                Last Updated: ${userData.updatedAt ?: "Unknown"}
            """.trimIndent()
            
            tvUserInfo.text = userInfo
        } else {
            tvUserInfo.text = "No user data found in Session Manager"
        }
    }
} 