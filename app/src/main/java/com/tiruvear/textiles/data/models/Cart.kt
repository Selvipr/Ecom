package com.tiruvear.textiles.data.models

import java.util.Date

data class Cart(
    val id: String,
    val userId: String,
    val createdAt: Date,
    val updatedAt: Date,
    
    // Relationships
    val items: List<CartItem>? = null
)

data class CartItem(
    val id: String,
    val cartId: String,
    val productId: String,
    val variantId: String?,
    val quantity: Int,
    val createdAt: Date,
    val updatedAt: Date,
    
    // Relationships
    val product: Product? = null,
    val variant: ProductVariant? = null
) 