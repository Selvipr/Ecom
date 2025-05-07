package com.tiruvear.textiles.data.models

import java.util.Date

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val basePrice: Double,
    val salePrice: Double?,
    val categoryId: String,
    val stockQuantity: Int,
    val isActive: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
    
    // Relationships
    val images: List<ProductImage>? = null,
    val category: ProductCategory? = null,
    val variants: List<ProductVariant>? = null
) {
    // Computed property to get the current price (sale price if available, otherwise base price)
    val price: Double
        get() = salePrice ?: basePrice
    
    // Computed property to get the first image URL or a default one
    val imageUrl: String
        get() = images?.firstOrNull()?.imageUrl ?: "https://placekitten.com/200/200" // Default placeholder image
} 