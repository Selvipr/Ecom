package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Order
import com.tiruvear.textiles.data.models.OrderItem
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.data.models.OrderStatus
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
    
    // Add method to get orders for current user
    suspend fun getUserOrders(): Result<List<Order>>
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
            // Return mock data if real data fetch fails
            val mockOrders = createMockOrders()
            Result.success(mockOrders)
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
    
    // Implementation of getUserOrders method
    override suspend fun getUserOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            // Get the current user ID from session or use a guest id
            val userId = TiruvearApp.getCurrentUserId() ?: "guest_user"
            
            // Return mock data for guest users
            if (userId == "guest_user") {
                return@withContext Result.success(createMockOrders())
            }
            
            // Reuse the getOrdersByUser method for authenticated users
            return@withContext getOrdersByUser(userId)
        } catch (e: Exception) {
            val mockOrders = createMockOrders()
            Result.success(mockOrders)
        }
    }
    
    // Mapping function
    private fun mapToOrder(data: Map<String, Any>): Order {
        @Suppress("UNCHECKED_CAST")
        val orderItems = (data["order_items"] as? List<Map<String, Any>>)?.map { item ->
            OrderItem(
                product = Product(
                    id = item["product_id"] as String,
                    name = "Unknown Product", // Placeholder, fetch real name if needed
                    description = "",
                    basePrice = (item["unit_price"] as? Number)?.toDouble() ?: 0.0,
                    salePrice = null,
                    categoryId = "",
                    stockQuantity = 0,
                    isActive = true,
                    createdAt = item["created_at"] as? java.util.Date ?: java.util.Date(),
                    updatedAt = item["created_at"] as? java.util.Date ?: java.util.Date()
                ),
                quantity = (item["quantity"] as Number).toInt(),
                price = (item["unit_price"] as? Number)?.toDouble() ?: 0.0
            )
        } ?: emptyList()
        
        return Order(
            id = data["id"] as String,
            userId = data["user_id"] as String,
            items = orderItems,
            totalAmount = (data["total_amount"] as Number).toDouble(),
            shippingAddress = data["shipping_address"] as String,
            status = OrderStatus.valueOf(data["status"] as String),
            paymentMethod = data["payment_method"] as String,
            orderDate = data["order_date"] as java.util.Date,
            deliveryDate = data["delivery_date"] as? java.util.Date,
            trackingNumber = data["tracking_number"] as? String
        )
    }
    
    // Helper method to create mock orders for testing
    private fun createMockOrders(): List<Order> {
        val currentDate = java.util.Date()
        val twoDaysAgo = java.util.Date(currentDate.time - (2 * 24 * 60 * 60 * 1000))
        val fiveDaysAgo = java.util.Date(currentDate.time - (5 * 24 * 60 * 60 * 1000))
        
        return listOf(
            Order(
                id = "ORD-001",
                userId = "guest_user",
                items = listOf(
                    OrderItem(
                        product = Product(
                            id = "P1",
                            name = "Cotton Saree",
                            description = "Beautiful cotton saree",
                            basePrice = 1499.0,
                            salePrice = 1299.0,
                            categoryId = "1",
                            stockQuantity = 10,
                            isActive = true,
                            createdAt = currentDate,
                            updatedAt = currentDate
                        ),
                        quantity = 1,
                        price = 1299.0
                    ),
                    OrderItem(
                        product = Product(
                            id = "P2",
                            name = "Silk Dhoti",
                            description = "Premium silk dhoti",
                            basePrice = 899.0,
                            salePrice = null,
                            categoryId = "2",
                            stockQuantity = 15,
                            isActive = true,
                            createdAt = currentDate,
                            updatedAt = currentDate
                        ),
                        quantity = 2,
                        price = 899.0
                    )
                ),
                totalAmount = 3097.0,
                shippingAddress = "123 Main St, Coimbatore, TN 641001",
                status = OrderStatus.DELIVERED,
                paymentMethod = "Cash on Delivery",
                orderDate = fiveDaysAgo,
                deliveryDate = currentDate,
                trackingNumber = "TN-12345"
            ),
            Order(
                id = "ORD-002",
                userId = "guest_user",
                items = listOf(
                    OrderItem(
                        product = Product(
                            id = "P3",
                            name = "Silk Saree",
                            description = "Premium silk saree",
                            basePrice = 2499.0,
                            salePrice = 2299.0,
                            categoryId = "1",
                            stockQuantity = 5,
                            isActive = true,
                            createdAt = currentDate,
                            updatedAt = currentDate
                        ),
                        quantity = 1,
                        price = 2299.0
                    )
                ),
                totalAmount = 2299.0,
                shippingAddress = "456 Temple St, Madurai, TN 625001",
                status = OrderStatus.SHIPPED,
                paymentMethod = "Credit Card",
                orderDate = twoDaysAgo,
                deliveryDate = null,
                trackingNumber = "TN-67890"
            ),
            Order(
                id = "ORD-003",
                userId = "guest_user",
                items = listOf(
                    OrderItem(
                        product = Product(
                            id = "P4",
                            name = "Kids Traditional Wear",
                            description = "Traditional wear for children",
                            basePrice = 899.0,
                            salePrice = 799.0,
                            categoryId = "6",
                            stockQuantity = 8,
                            isActive = true,
                            createdAt = currentDate,
                            updatedAt = currentDate
                        ),
                        quantity = 2,
                        price = 799.0
                    )
                ),
                totalAmount = 1598.0,
                shippingAddress = "789 River Rd, Chennai, TN 600001",
                status = OrderStatus.PENDING,
                paymentMethod = "UPI",
                orderDate = currentDate,
                deliveryDate = null,
                trackingNumber = null
            )
        )
    }
} 