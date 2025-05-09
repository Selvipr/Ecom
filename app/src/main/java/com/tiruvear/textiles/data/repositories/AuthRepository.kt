package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.data.models.LoginRequest
import com.tiruvear.textiles.data.models.LoginResponse
import com.tiruvear.textiles.data.models.User
import java.io.File

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse>
    suspend fun register(user: User, password: String): Result<LoginResponse>
    suspend fun resetPassword(email: String): Result<Boolean>
    suspend fun logout(): Result<Boolean>
    suspend fun getUserProfile(userId: String): Result<User>
    suspend fun updateUserProfile(user: User): Result<User>
    suspend fun uploadProfileImage(file: File, fileName: String): Result<String>
} 