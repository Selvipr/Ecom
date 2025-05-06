package com.tiruvear.textiles.data.models

import java.util.Date

data class ProductImage(
    val id: String,
    val productId: String,
    val imageUrl: String,
    val isPrimary: Boolean,
    val displayOrder: Int,
    val createdAt: Date
) 