package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.User
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, firstName: String, lastName: String, phone: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): Result<User?>
    suspend fun forgotPassword(email: String): Result<Unit>
}

class AuthRepositoryImpl : AuthRepository {
    
    private val supabase = TiruvearApp.supabaseClient
    private val auth = supabase.gotrue
    private val postgrest = supabase.postgrest
    
    override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Login with email and password
            auth.loginWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Get user ID from session
            val session = auth.currentSessionOrNull()
            val userId = session?.user?.id ?: ""
            
            val user = fetchUserDetails(userId)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(email: String, password: String, firstName: String, lastName: String, phone: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            println("REGISTRATION DEBUG: Starting registration process for email: $email")
            
            // Validate input
            if (email.isBlank() || password.isBlank()) {
                println("REGISTRATION DEBUG: Email or password is blank")
                return@withContext Result.failure(Exception("Email and password are required"))
            }
            
            // Register with email and password
            try {
                println("REGISTRATION DEBUG: Attempting Supabase signUp")
                val signUpResponse = auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                println("REGISTRATION DEBUG: Signup successful, response: $signUpResponse")
            } catch (e: Exception) {
                println("REGISTRATION DEBUG: Error during signUpWith: ${e.message}")
                println("REGISTRATION DEBUG: Stack trace: ${e.stackTraceToString()}")
                if (e.message?.contains("User already registered") == true) {
                    return@withContext Result.failure(Exception("Email is already registered. Please use a different email or try logging in."))
                }
                return@withContext Result.failure(Exception("Registration error: ${e.message}"))
            }
            
            // Get user ID from session
            println("REGISTRATION DEBUG: Checking for session after signup")
            var session = auth.currentSessionOrNull()
            val userId = session?.user?.id
            
            println("REGISTRATION DEBUG: Initial session user ID: $userId")
            
            if (userId.isNullOrEmpty()) {
                println("REGISTRATION DEBUG: No user ID in session after signup, attempting login")
                // Try to sign in with the credentials in case user was created but session wasn't established 
                try {
                    auth.loginWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    println("REGISTRATION DEBUG: Login successful after signup")
                    session = auth.currentSessionOrNull()
                    val retrievedUserId = session?.user?.id
                    
                    println("REGISTRATION DEBUG: Retrieved user ID after login: $retrievedUserId")
                    
                    if (retrievedUserId.isNullOrEmpty()) {
                        return@withContext Result.failure(Exception("Failed to create user account. Please try again later."))
                    }
                } catch (e: Exception) {
                    println("REGISTRATION DEBUG: Error during login after signup: ${e.message}")
                    println("REGISTRATION DEBUG: Stack trace: ${e.stackTraceToString()}")
                    return@withContext Result.failure(Exception("Account creation failed: ${e.message}"))
                }
            }
            
            val finalUserId = session?.user?.id ?: ""
            println("REGISTRATION DEBUG: Final user ID: $finalUserId")
            
            if (finalUserId.isEmpty()) {
                println("REGISTRATION DEBUG: Final user ID is empty, returning failure")
                return@withContext Result.failure(Exception("Failed to obtain user ID. Please contact support."))
            }
            
            // Create user profile
            println("REGISTRATION DEBUG: Creating user profile")
            val now = Date()
            val userData = mapOf(
                "id" to finalUserId,
                "email" to email,
                "first_name" to firstName,
                "last_name" to lastName,
                "phone" to phone,
                "created_at" to now.toString(),
                "updated_at" to now.toString()
            )
            
            println("REGISTRATION DEBUG: Inserting user data: $userData")
            
            // Insert user data into users table
            try {
                postgrest["users"].insert(userData)
                println("REGISTRATION DEBUG: User data inserted successfully")
            } catch (e: Exception) {
                println("REGISTRATION DEBUG: Error inserting user data: ${e.message}")
                println("REGISTRATION DEBUG: Stack trace: ${e.stackTraceToString()}")
                // We'll still return success since the account was created, even if profile data failed
                val basicUser = User(
                    id = finalUserId,
                    email = email,
                    phone = phone,
                    firstName = firstName,
                    lastName = lastName,
                    createdAt = now,
                    updatedAt = now
                )
                return@withContext Result.success(basicUser)
            }
            
            println("REGISTRATION DEBUG: Fetching complete user profile")
            try {
                val user = fetchUserDetails(finalUserId)
                println("REGISTRATION DEBUG: Registration complete, returning user")
                Result.success(user)
            } catch (e: Exception) {
                println("REGISTRATION DEBUG: Error fetching user details: ${e.message}")
                // Return basic user info if we couldn't fetch the complete profile
                val basicUser = User(
                    id = finalUserId,
                    email = email,
                    phone = phone,
                    firstName = firstName,
                    lastName = lastName,
                    createdAt = now,
                    updatedAt = now
                )
                Result.success(basicUser)
            }
        } catch (e: Exception) {
            println("REGISTRATION DEBUG: Uncaught error in register: ${e.message}")
            println("REGISTRATION DEBUG: Stack trace: ${e.stackTraceToString()}")
            
            // Check for rate limiting errors
            val message = e.message ?: ""
            if (message.contains("For security purposes") || message.contains("rate limited") || message.contains("too many requests")) {
                return@withContext Result.failure(Exception("Registration rate limited. Please wait 1 minute before trying again."))
            }
            
            // Include the error message for better debugging
            Result.failure(Exception("Registration failed: ${e.message}"))
        }
    }
    
    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        auth.currentSessionOrNull() != null
    }
    
    override suspend fun getCurrentUser(): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val session = auth.currentSessionOrNull() ?: return@withContext Result.success(null)
            val userId = session.user?.id ?: return@withContext Result.success(null)
            
            val user = fetchUserDetails(userId)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun forgotPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Use the correct method for sending password recovery email
            auth.sendRecoveryEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun fetchUserDetails(userId: String): User {
        try {
            println("Fetching user details for ID: $userId")
            
            // Query user details from database
            val userData = postgrest["users"]
                .select {
                    filter("id", FilterOperator.EQ, userId)
                }
                .decodeSingle<JsonObject>()
            
            println("User data fetched: $userData")
            
            return User(
                id = userData["id"]?.jsonPrimitive?.content ?: "",
                email = userData["email"]?.jsonPrimitive?.content ?: "",
                phone = userData["phone"]?.jsonPrimitive?.content ?: "",
                firstName = userData["first_name"]?.jsonPrimitive?.content ?: "",
                lastName = userData["last_name"]?.jsonPrimitive?.content ?: "",
                createdAt = Date(),
                updatedAt = Date()
            )
        } catch (e: Exception) {
            println("Error fetching user details: ${e.message}")
            
            // Try to get basic user info from session
            val session = auth.currentSessionOrNull()
            val email = session?.user?.email ?: ""
            
            // Return basic user if profile doesn't exist
            return User(
                id = userId,
                email = email,
                phone = "",
                firstName = "",
                lastName = "",
                createdAt = Date(),
                updatedAt = Date()
            )
        }
    }
} 