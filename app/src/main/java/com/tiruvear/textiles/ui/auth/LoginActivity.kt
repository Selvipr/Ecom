package com.tiruvear.textiles.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tiruvear.textiles.data.models.LoginRequest
import com.tiruvear.textiles.data.repositories.AuthRepositoryImpl
import com.tiruvear.textiles.data.util.SessionManager
import com.tiruvear.textiles.databinding.ActivityLoginBinding
import com.tiruvear.textiles.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Added to resolve reference errors
class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Placeholder - implement this class properly
        finish()
    }
}

// Added to resolve reference errors
class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Placeholder - implement this class properly
        finish()
    }
}

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val authRepository by lazy { AuthRepositoryImpl() }
    private lateinit var sessionManager: SessionManager
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        
        // Check if user is already logged in and remember me is enabled
        if (sessionManager.isUserLoggedIn() && sessionManager.isRememberMe()) {
            navigateToMain()
            return
        }
        
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        // If remember me was previously checked, pre-fill the credentials
        if (sessionManager.isRememberMe()) {
            sessionManager.getUserData()?.let { user ->
                binding.etEmail.setText(user.email)
                binding.cbRememberMe.isChecked = true
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                login()
            }
        }
        
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
    
    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return false
        }
        
        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return false
        }
        
        return true
    }
    
    private fun login() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val rememberMe = binding.cbRememberMe.isChecked
        
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        
        coroutineScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                
                val result = withContext(Dispatchers.IO) {
                    authRepository.login(loginRequest)
                }
                
                if (result.isSuccess) {
                    val loginResponse = result.getOrNull()
                    
                    if (loginResponse != null) {
                        // Save user session
                        sessionManager.setUserLoggedIn(true)
                        sessionManager.setRememberMe(rememberMe)
                        sessionManager.saveUserId(loginResponse.user.id)
                        sessionManager.saveUserToken(loginResponse.token)
                        sessionManager.saveUserData(loginResponse.user)
                        
                        // Navigate to main screen
                        navigateToMain()
                    } else {
                        showError("Login failed. Please try again.")
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e("LoginActivity", "Login error: ${exception?.message}", exception)
                    showError(exception?.message ?: "Login failed. Please try again.")
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Exception during login: ${e.message}", e)
                showError("An error occurred. Please try again.")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
            }
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
} 