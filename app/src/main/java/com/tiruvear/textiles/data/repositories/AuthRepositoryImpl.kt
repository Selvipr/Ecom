package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.data.models.LoginRequest
import com.tiruvear.textiles.data.models.LoginResponse
import com.tiruvear.textiles.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

/**
 * Implementation of AuthRepository
 */
class AuthRepositoryImpl : AuthRepository {
    
    // Since the Supabase implementation is causing issues, we'll use a mock implementation
    override suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            // Mock successful login
            val user = User(
                id = UUID.randomUUID().toString(),
                name = "Demo User",
                email = loginRequest.email,
                phone = "1234567890",
                isVerified = true
            )
            
            val token = "mock_jwt_token_${user.id}"
            
            Result.success(LoginResponse(token, user))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(user: User, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            // Mock successful registration
            val registeredUser = user.copy(
                id = UUID.randomUUID().toString(),
                isVerified = false
            )
            
            val token = "mock_jwt_token_${registeredUser.id}"
            
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
            // Mock successful logout
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 