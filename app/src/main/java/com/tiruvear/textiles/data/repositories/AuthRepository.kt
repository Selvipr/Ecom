package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.User
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
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
            val response = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = response?.id ?: return@withContext Result.failure(Exception("No user ID found"))
            val user = fetchUserDetails(userId)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(email: String, password: String, firstName: String, lastName: String, phone: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Simple signup with email/password
            val response = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = response?.id ?: return@withContext Result.failure(Exception("No user ID found"))
            
            // Insert user profile into users table
            val now = Date()
            postgrest["users"].insert(
                mapOf(
                    "id" to userId,
                    "email" to email,
                    "first_name" to firstName,
                    "last_name" to lastName,
                    "phone" to phone,
                    "created_at" to now.toString(),
                    "updated_at" to now.toString()
                )
            )
            
            val user = fetchUserDetails(userId)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
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
        auth.currentUserOrNull() != null
    }
    
    override suspend fun getCurrentUser(): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUserOrNull() ?: return@withContext Result.success(null)
            val userId = currentUser.id ?: return@withContext Result.success(null)
            
            val user = fetchUserDetails(userId)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun forgotPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Use the sendRecoveryEmail method instead of resetPasswordForEmail
            auth.sendRecoveryEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun fetchUserDetails(userId: String): User {
        // Use a more basic query approach
        val userData = postgrest["users"]
            .select {
                // Build a simple query to select by id
                eq("id", userId)
            }
            .decodeSingle<JsonObject>()
        
        return User(
            id = userData["id"]?.jsonPrimitive?.content ?: "",
            email = userData["email"]?.jsonPrimitive?.content ?: "",
            phone = userData["phone"]?.jsonPrimitive?.content ?: "",
            firstName = userData["first_name"]?.jsonPrimitive?.content ?: "",
            lastName = userData["last_name"]?.jsonPrimitive?.content ?: "",
            createdAt = Date(), // Parse the date string properly in a real implementation
            updatedAt = Date()  // Parse the date string properly in a real implementation
        )
    }
} 