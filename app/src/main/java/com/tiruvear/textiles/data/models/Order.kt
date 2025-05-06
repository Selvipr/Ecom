package com.tiruvear.textiles.data.models

import java.util.Date

data class Order(
    val id: String,
    val userId: String,
    val addressId: String,
    val status: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val shippingCharge: Double,
    val discountAmount: Double,
    val createdAt: Date,
    val updatedAt: Date,
    
    // Relationships
    val items: List<OrderItem>? = null,
    val address: Address? = null
)

data class OrderItem(
    val id: String,
    val orderId: String,
    val productId: String,
    val variantId: String?,
    val quantity: Int,
    val unitPrice: Double,
    val createdAt: Date,
    
    // Relationships
    val product: Product? = null,
    val variant: ProductVariant? = null
) 