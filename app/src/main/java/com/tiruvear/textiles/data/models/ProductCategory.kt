package com.tiruvear.textiles.data.models

import java.util.Date

data class ProductCategory(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val parentCategoryId: String?,
    val createdAt: Date,
    val updatedAt: Date
) 