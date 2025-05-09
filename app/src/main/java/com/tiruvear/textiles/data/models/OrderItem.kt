package com.tiruvear.textiles.data.models

/**
 * Represents an item in an order
 */
data class OrderItem(
    val product: Product,
    val quantity: Int,
    val price: Double
) 