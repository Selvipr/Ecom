package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Order
import com.tiruvear.textiles.data.models.OrderItem
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import io.github.jan.supabase.postgrest.query.Order as QueryOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

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
}

class OrderRepositoryImpl : OrderRepository {
    
    private val supabase = TiruvearApp.supabaseClient
    
    override suspend fun createOrder(
        userId: String,
        addressId: String,
        cartId: String,
        paymentMethod: String,
        shippingCharge: Double,
        discountAmount: Double
    ): Result<Order> = withContext(Dispatchers.IO) {
        try {
            // Get cart items to calculate total
            val cartItems = supabase.postgrest["cart_items"]
                .select {
                    filter("cart_id", FilterOperator.EQ, cartId)
                }
                .decodeList<Map<String, Any>>()
            
            // Calculate total amount (simplified example)
            val totalAmount = cartItems.sumOf { (it["quantity"] as Number).toInt() * (it["price"] as Number).toDouble() }
            
            // Create order
            val orderId = UUID.randomUUID().toString()
            val now = Date()
            
            val orderData = mapOf(
                "id" to orderId,
                "user_id" to userId,
                "address_id" to addressId,
                "status" to "pending",
                "total_amount" to totalAmount,
                "payment_method" to paymentMethod,
                "payment_status" to "pending",
                "shipping_charge" to shippingCharge,
                "discount_amount" to discountAmount,
                "created_at" to now,
                "updated_at" to now
            )
            
            supabase.postgrest["orders"].insert(orderData)
            
            // Create order items from cart items
            for (item in cartItems) {
                val orderItemId = UUID.randomUUID().toString()
                val orderItemData = mapOf(
                    "id" to orderItemId,
                    "order_id" to orderId,
                    "product_id" to item["product_id"],
                    "variant_id" to item["variant_id"],
                    "quantity" to item["quantity"],
                    "unit_price" to item["price"],
                    "created_at" to now
                )
                
                supabase.postgrest["order_items"].insert(orderItemData)
            }
            
            // Clear cart
            supabase.postgrest["cart_items"]
                .delete {
                    filter("cart_id", FilterOperator.EQ, cartId)
                }
            
            // Return the created order
            return@withContext getOrderById(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOrderById(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val orderData = supabase.postgrest["orders"]
                .select {
                    filter("id", FilterOperator.EQ, orderId)
                }
                .decodeSingle<Map<String, Any>>()
                
            // Get order items
            val orderItems = supabase.postgrest["order_items"]
                .select {
                    filter("order_id", FilterOperator.EQ, orderId)
                }
                .decodeList<Map<String, Any>>()
                
            // Add items to order data
            val fullOrderData = orderData.toMutableMap()
            fullOrderData["order_items"] = orderItems
                
            // Process the order data
            Result.success(mapToOrder(fullOrderData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOrdersByUser(userId: String): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val orders = supabase.postgrest["orders"]
                .select {
                    filter("user_id", FilterOperator.EQ, userId)
                    order("created_at", QueryOrder.DESCENDING)
                }
                .decodeList<Map<String, Any>>()
                
            // For each order, get its items
            val ordersWithItems = orders.map { orderData ->
                val orderId = orderData["id"] as String
                val orderItems = supabase.postgrest["order_items"]
                    .select {
                        filter("order_id", FilterOperator.EQ, orderId)
                    }
                    .decodeList<Map<String, Any>>()
                
                val fullOrderData = orderData.toMutableMap()
                fullOrderData["order_items"] = orderItems
                fullOrderData
            }
                
            Result.success(ordersWithItems.map { mapToOrder(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val now = Date()
            val updateData = mapOf(
                "status" to status,
                "updated_at" to now
            )
            
            supabase.postgrest["orders"]
                .update(updateData) {
                    filter("id", FilterOperator.EQ, orderId)
                }
            
            return@withContext getOrderById(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cancelOrder(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val now = Date()
            val updateData = mapOf(
                "status" to "cancelled",
                "updated_at" to now
            )
            
            supabase.postgrest["orders"]
                .update(updateData) {
                    filter("id", FilterOperator.EQ, orderId)
                }
            
            return@withContext getOrderById(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Mapping function
    private fun mapToOrder(data: Map<String, Any>): Order {
        @Suppress("UNCHECKED_CAST")
        val orderItems = (data["order_items"] as? List<Map<String, Any>>)?.map { item ->
            OrderItem(
                id = item["id"] as String,
                orderId = item["order_id"] as String,
                productId = item["product_id"] as String,
                variantId = item["variant_id"] as? String,
                quantity = (item["quantity"] as Number).toInt(),
                unitPrice = (item["unit_price"] as Number).toDouble(),
                createdAt = item["created_at"] as Date,
                product = null,
                variant = null
            )
        } ?: emptyList()
        
        return Order(
            id = data["id"] as String,
            userId = data["user_id"] as String,
            addressId = data["address_id"] as String,
            status = data["status"] as String,
            totalAmount = (data["total_amount"] as Number).toDouble(),
            paymentMethod = data["payment_method"] as String,
            paymentStatus = data["payment_status"] as String,
            shippingCharge = (data["shipping_charge"] as Number).toDouble(),
            discountAmount = (data["discount_amount"] as Number).toDouble(),
            createdAt = data["created_at"] as Date,
            updatedAt = data["updated_at"] as Date,
            items = orderItems,
            address = null
        )
    }
} 