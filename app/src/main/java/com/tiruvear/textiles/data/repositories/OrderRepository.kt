package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.data.models.Order

interface OrderRepository {
    suspend fun createOrder(
        userId: String,
        addressId: String,
        cartId: String,
        paymentMethod: String,
        shippingCharge: Double,
        discountAmount: Double
    ): Result<Order>
    
    suspend fun getOrderById(orderId: String): Result<Order>
    suspend fun getOrdersByUser(userId: String): Result<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Order>
    suspend fun cancelOrder(orderId: String): Result<Order>
    
    // Add method to get orders for current user
    suspend fun getUserOrders(): Result<List<Order>>
} 