package com.tiruvear.textiles.data.models

import java.util.Date

data class Review(
    val id: String,
    val productId: String,
    val userId: String,
    val rating: Int,
    val comment: String?,
    val createdAt: Date,
    val updatedAt: Date,
    
    // Relationships
    val user: User? = null
) 