package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.data.models.LoginRequest
import com.tiruvear.textiles.data.models.LoginResponse
import com.tiruvear.textiles.data.models.User

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse>
    suspend fun register(user: User, password: String): Result<LoginResponse>
    suspend fun resetPassword(email: String): Result<Boolean>
    suspend fun logout(): Result<Boolean>
} 