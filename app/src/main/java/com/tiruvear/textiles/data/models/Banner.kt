package com.tiruvear.textiles.data.models

/**
 * Data class to represent promotional banners in the app
 */
data class Banner(
    val id: Int,
    val title: String,
    val description: String,
    val imageResId: Int // For simplicity using resource IDs, in real app would use URLs
) 