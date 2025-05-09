package com.tiruvear.textiles.data.models

/**
 * Model class representing a user in the app
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val isVerified: Boolean = false,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) 