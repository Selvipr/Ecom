package com.tiruvear.textiles.data.models

import java.util.Date

data class ProductCategory(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val parentCategoryId: String? = null,
    val displayOrder: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 