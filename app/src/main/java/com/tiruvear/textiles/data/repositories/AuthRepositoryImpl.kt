package com.tiruvear.textiles.data.repositories

import android.content.Context
import android.util.Log
import com.tiruvear.textiles.data.models.LoginRequest
import com.tiruvear.textiles.data.models.LoginResponse
import com.tiruvear.textiles.data.models.User
import com.tiruvear.textiles.data.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Implementation of authentication repository for handling user authentication
 */
class AuthRepositoryImpl(private val context: Context? = null) : AuthRepository {
    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }
    
    private val sessionManager by lazy { 
        context?.let { SessionManager(it) }
    }
    
    // Mock implementation
    override suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            // Check if the email and password are valid
            if (loginRequest.email.isBlank() || !isValidEmail(loginRequest.email)) {
                return@withContext Result.failure(Exception("Invalid email format"))
            }
            
            if (loginRequest.password.isBlank() || loginRequest.password.length < 6) {
                return@withContext Result.failure(Exception("Password must be at least 6 characters"))
            }
            
            // For demo purposes, allow these test credentials or any email with a proper password length
            val validTestEmail = "demo@example.com"
            val validTestPassword = "password123"
            
            if ((loginRequest.email == validTestEmail && loginRequest.password == validTestPassword) ||
                (isValidEmail(loginRequest.email) && loginRequest.password.length >= 6)) {
                // Create a user with the email from the request
                val nameFromEmail = loginRequest.email.split("@").first().capitalize()
                val user = User(
                    id = UUID.randomUUID().toString(),
                    name = "Demo User ($nameFromEmail)",
                    email = loginRequest.email,
                    phone = "1234567890",
                    isVerified = true
                )
                
                val token = "mock_jwt_token_${user.id}"
                
                // Save user to session manager
                sessionManager?.saveUserData(user)
                sessionManager?.saveUserToken(token)
                sessionManager?.saveUserId(user.id)
                sessionManager?.setUserLoggedIn(true)
                
                Result.success(LoginResponse(token, user))
            } else {
                Result.failure(Exception("Invalid credentials. Try demo@example.com with password123"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
    
    override suspend fun register(user: User, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            // Mock successful registration
            val registeredUser = user.copy(
                id = UUID.randomUUID().toString(),
                isVerified = false
            )
            
            val token = "mock_jwt_token_${registeredUser.id}"
            
            // Save user to session manager
            sessionManager?.saveUserData(registeredUser)
            sessionManager?.saveUserToken(token)
            sessionManager?.saveUserId(registeredUser.id)
            sessionManager?.setUserLoggedIn(true)
            
            Result.success(LoginResponse(token, registeredUser))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun resetPassword(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Mock successful password reset
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Clear session
            sessionManager?.clearSession()
            
            // Mock successful logout
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetches user profile from local storage
     */
    override suspend fun getUserProfile(userId: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // First try to get the user from SessionManager
            val sessionUser = sessionManager?.getUserData()
            if (sessionUser != null && sessionUser.id == userId) {
                return@withContext Result.success(sessionUser)
            }
            
            // Fall back to mock data if not in session
            Result.success(getLocalUserProfile(userId))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Updates the user profile in local storage
     */
    override suspend fun updateUserProfile(user: User): Result<User> = withContext(Dispatchers.IO) {
        try {
            val currentTimestamp = System.currentTimeMillis()
            val updatedUser = user.copy(updatedAt = currentTimestamp)
            
            // Save to session manager
            sessionManager?.saveUserData(updatedUser)
            
            // Also save locally for backup
            saveLocalUserProfile(updatedUser)
            
            Result.success(updatedUser)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Saves profile image to local storage and returns a local file URL
     */
    override suspend fun uploadProfileImage(file: File, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val localUrl = "file://${file.absolutePath}"
            saveLocalImageUrl(fileName, localUrl)
            Result.success(localUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile image: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Local data storage methods
    
    private fun getLocalUserProfile(userId: String): User {
        return User(
            id = userId,
            name = "Demo User",
            email = "user@example.com",
            phone = "9876543210",
            profileImageUrl = null,
            isVerified = true,
            createdAt = System.currentTimeMillis() - 86400000, // Yesterday
            updatedAt = System.currentTimeMillis()
        )
    }
    
    private fun saveLocalUserProfile(user: User) {
        // In a real app, you would save this to shared preferences or a local database
        Log.d(TAG, "Locally saved user profile: $user")
    }
    
    private fun saveLocalImageUrl(fileName: String, url: String) {
        // In a real app, you would save this to shared preferences or a local database
        Log.d(TAG, "Locally saved image URL for $fileName: $url")
    }
} 