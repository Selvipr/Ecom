package com.tiruvear.textiles.data.models

data class CartItem(
    val id: String,
    val product: Product,
    val quantity: Int,
    val price: Double,
    val cartId: String
) {
    // Computed property to get the total price for this item
    val totalPrice: Double
        get() = price * quantity
} 