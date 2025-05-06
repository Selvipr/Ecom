package com.tiruvear.textiles.data.models

import java.util.Date

data class ProductVariant(
    val id: String,
    val productId: String,
    val variantType: String,
    val variantValue: String,
    val priceAdjustment: Double,
    val stockQuantity: Int,
    val createdAt: Date,
    val updatedAt: Date
) 