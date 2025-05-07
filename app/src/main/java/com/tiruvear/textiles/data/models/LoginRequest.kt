package com.tiruvear.textiles.data.models

/**
 * Data class for login request
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Data class for login response
 */
data class LoginResponse(
    val token: String,
    val user: User
) 