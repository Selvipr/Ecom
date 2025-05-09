package com.tiruvear.textiles.data.models

import java.util.Date

/**
 * Model class representing a customer order
 */
data class Order(
    val id: String,
    val userId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val shippingAddress: String,
    val status: OrderStatus,
    val paymentMethod: String,
    val orderDate: Date,
    val deliveryDate: Date? = null,
    val trackingNumber: String? = null
)

/**
 * Enum representing possible order statuses
 */
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
} 