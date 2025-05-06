package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Order
import com.tiruvear.textiles.data.models.OrderItem
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from
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
            // Get cart items
            val cartItems = supabase.from("cart_items")
                .select("*, products(*), product_variants(*)")
                .eq("cart_id", cartId)
                .decodeList<Map<String, Any>>()
            
            if (cartItems.isEmpty()) {
                return@withContext Result.failure(Exception("Cart is empty"))
            }
            
            // Calculate total amount
            var totalAmount = 0.0
            
            for (item in cartItems) {
                val product = (item["products"] as Map<String, Any>)
                val quantity = (item["quantity"] as Number).toInt()
                var price = (product["base_price"] as Number).toDouble()
                
                // Apply variant price adjustment if any
                val variant = item["product_variants"] as? Map<String, Any>
                if (variant != null) {
                    val adjustment = (variant["price_adjustment"] as Number).toDouble()
                    price += adjustment
                }
                
                // Apply sale price if available
                val salePrice = (product["sale_price"] as? Number)?.toDouble()
                if (salePrice != null && salePrice > 0) {
                    price = salePrice
                }
                
                totalAmount += price * quantity
            }
            
            // Add shipping, subtract discount
            totalAmount += shippingCharge
            totalAmount -= discountAmount
            
            // Create order
            val orderId = UUID.randomUUID().toString()
            val now = Date()
            
            val orderValues = mapOf(
                "id" to orderId,
                "user_id" to userId,
                "address_id" to addressId,
                "status" to "PLACED",
                "total_amount" to totalAmount,
                "payment_method" to paymentMethod,
                "payment_status" to if (paymentMethod == "CASH_ON_DELIVERY") "PENDING" else "PAID",
                "shipping_charge" to shippingCharge,
                "discount_amount" to discountAmount,
                "created_at" to now,
                "updated_at" to now
            )
            
            supabase.from("orders").insert(values = orderValues)
            
            // Create order items
            for (item in cartItems) {
                val productId = item["product_id"] as String
                val variantId = item["variant_id"] as? String
                val quantity = (item["quantity"] as Number).toInt()
                
                val product = (item["products"] as Map<String, Any>)
                var unitPrice = (product["base_price"] as Number).toDouble()
                
                // Apply variant price adjustment if any
                val variant = item["product_variants"] as? Map<String, Any>
                if (variant != null) {
                    val adjustment = (variant["price_adjustment"] as Number).toDouble()
                    unitPrice += adjustment
                }
                
                // Apply sale price if available
                val salePrice = (product["sale_price"] as? Number)?.toDouble()
                if (salePrice != null && salePrice > 0) {
                    unitPrice = salePrice
                }
                
                val orderItemId = UUID.randomUUID().toString()
                val orderItemValues = mutableMapOf(
                    "id" to orderItemId,
                    "order_id" to orderId,
                    "product_id" to productId,
                    "quantity" to quantity,
                    "unit_price" to unitPrice,
                    "created_at" to now
                )
                
                if (variantId != null) {
                    orderItemValues["variant_id"] = variantId
                }
                
                supabase.from("order_items").insert(values = orderItemValues)
            }
            
            // Clear cart
            supabase.from("cart_items").delete {
                eq("cart_id", cartId)
            }
            
            // Return the created order
            return@withContext getOrderById(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOrderById(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val orderData = supabase.from("orders")
                .select("*, addresses(*)")
                .eq("id", orderId)
                .single()
                .decodeAs<Map<String, Any>>()
            
            val orderItems = supabase.from("order_items")
                .select("*, products(*), product_variants(*)")
                .eq("order_id", orderId)
                .decodeList<Map<String, Any>>()
                .map { item -> mapToOrderItem(item) }
            
            val order = mapToOrder(orderData)
            
            // Add items to the order
            val orderWithItems = order.copy(items = orderItems)
            
            Result.success(orderWithItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOrdersByUser(userId: String): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val orders = supabase.from("orders")
                .select("*, addresses(*)")
                .eq("user_id", userId)
                .order("created_at", ascending = false)
                .decodeList<Map<String, Any>>()
                .map { item -> mapToOrder(item) }
            
            for (order in orders) {
                val orderItems = supabase.from("order_items")
                    .select("*, products(*), product_variants(*)")
                    .eq("order_id", order.id)
                    .decodeList<Map<String, Any>>()
                    .map { item -> mapToOrderItem(item) }
            }
            
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            val now = Date()
            
            supabase.from("orders").update(
                values = mapOf(
                    "status" to status,
                    "updated_at" to now
                )
            ) {
                eq("id", orderId)
            }
            
            return@withContext getOrderById(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cancelOrder(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            return@withContext updateOrderStatus(orderId, "CANCELLED")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun mapToOrder(data: Map<String, Any>): Order {
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
            updatedAt = data["updated_at"] as Date
            // Note: items and address relationships need more complex mapping
        )
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun mapToOrderItem(data: Map<String, Any>): OrderItem {
        return OrderItem(
            id = data["id"] as String,
            orderId = data["order_id"] as String,
            productId = data["product_id"] as String,
            variantId = data["variant_id"] as? String,
            quantity = (data["quantity"] as Number).toInt(),
            unitPrice = (data["unit_price"] as Number).toDouble(),
            createdAt = data["created_at"] as Date
            // Note: Product and variant relationships need more complex mapping
        )
    }
} 