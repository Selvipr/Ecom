package com.tiruvear.textiles.data.repositories

import android.util.Log
import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Order
import com.tiruvear.textiles.data.models.OrderItem
import com.tiruvear.textiles.data.models.OrderStatus
import com.tiruvear.textiles.data.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import java.util.Calendar

class OrderRepositoryImpl : OrderRepository {
    
    private val TAG = "OrderRepositoryImpl"
    
    // In a real app, this would be connected to a database
    private val mockOrders = createMockOrders()
    
    override suspend fun getOrderById(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            // For this example app, we'll just return mock data
            val order = mockOrders.find { it.id == orderId }
            if (order != null) {
                Result.success(order)
            } else {
                Log.e(TAG, "Order not found with ID: $orderId")
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting order: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getOrdersByUser(userId: String): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            // For this example app, we'll just return mock data
            Result.success(mockOrders)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting orders by user: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            // Find the order to update
            val orderIndex = mockOrders.indexOfFirst { it.id == orderId }
            if (orderIndex >= 0) {
                val oldOrder = mockOrders[orderIndex]
                val newStatus = OrderStatus.valueOf(status.uppercase())
                
                // Create updated order with new status
                val updatedOrder = oldOrder.copy(status = newStatus)
                
                // Update our local list (in a real app, this would update the database)
                val mutableList = mockOrders.toMutableList()
                mutableList[orderIndex] = updatedOrder
                
                Result.success(updatedOrder)
            } else {
                Log.e(TAG, "Order not found for status update: $orderId")
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun cancelOrder(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            // Find the order to update
            val orderIndex = mockOrders.indexOfFirst { it.id == orderId }
            if (orderIndex >= 0) {
                val oldOrder = mockOrders[orderIndex]
                
                // Create updated order with cancelled status
                val updatedOrder = oldOrder.copy(status = OrderStatus.CANCELLED)
                
                // Update our local list (in a real app, this would update the database)
                val mutableList = mockOrders.toMutableList()
                mutableList[orderIndex] = updatedOrder
                
                Result.success(updatedOrder)
            } else {
                Log.e(TAG, "Order not found for cancellation: $orderId")
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling order: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getUserOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            // For a demo app, just return mock orders
            Result.success(mockOrders)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user orders: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun createOrder(
        userId: String, 
        addressId: String, 
        cartId: String, 
        paymentMethod: String, 
        shippingCharge: Double, 
        discountAmount: Double
    ): Result<Order> = withContext(Dispatchers.IO) {
        try {
            // In a real app, this would create an order in the database
            // For our example, we'll just create a mock order
            val orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8)
            val order = Order(
                id = orderId,
                userId = userId,
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
                            createdAt = Date(),
                            updatedAt = Date()
                        ),
                        quantity = 1,
                        price = 1299.0
                    )
                ),
                totalAmount = 1299.0 + shippingCharge - discountAmount,
                shippingAddress = "123 Main St, Coimbatore, TN 641001",
                status = OrderStatus.PENDING,
                paymentMethod = paymentMethod,
                orderDate = Date(),
                deliveryDate = null,
                trackingNumber = null
            )
            
            // Add to our mock list
            val mutableList = mockOrders.toMutableList()
            mutableList.add(0, order)
            
            Result.success(order)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Helper method to create mock orders for testing
    private fun createMockOrders(): List<Order> {
        val cal = Calendar.getInstance()
        val currentDate = cal.time
        
        // Create dates for various orders
        cal.add(Calendar.DAY_OF_MONTH, -2) 
        val twoDaysAgo = cal.time
        
        cal.add(Calendar.DAY_OF_MONTH, -3)
        val fiveDaysAgo = cal.time
        
        cal.add(Calendar.DAY_OF_MONTH, -5)
        val tenDaysAgo = cal.time
        
        cal.add(Calendar.DAY_OF_MONTH, -10)
        val twentyDaysAgo = cal.time
        
        // Create a future date for delivery
        cal.setTime(currentDate)
        cal.add(Calendar.DAY_OF_MONTH, 3)
        val deliveryDate = cal.time
        
        return listOf(
            Order(
                id = "ORD-12345678",
                userId = "user123",
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
                status = OrderStatus.PROCESSING,
                paymentMethod = "Cash on Delivery",
                orderDate = currentDate,
                deliveryDate = null,
                trackingNumber = null
            ),
            Order(
                id = "ORD-23456789",
                userId = "user123",
                items = listOf(
                    OrderItem(
                        product = Product(
                            id = "P3",
                            name = "Fancy Saree",
                            description = "Elegant fancy saree",
                            basePrice = 2499.0,
                            salePrice = 1999.0,
                            categoryId = "1",
                            stockQuantity = 5,
                            isActive = true,
                            createdAt = twoDaysAgo,
                            updatedAt = twoDaysAgo
                        ),
                        quantity = 1,
                        price = 1999.0
                    )
                ),
                totalAmount = 1999.0,
                shippingAddress = "456 Oak St, Chennai, TN 600001",
                status = OrderStatus.SHIPPED,
                paymentMethod = "Credit Card",
                orderDate = twoDaysAgo,
                deliveryDate = deliveryDate,
                trackingNumber = "TN-987654321"
            ),
            Order(
                id = "ORD-34567890",
                userId = "user123",
                items = listOf(
                    OrderItem(
                        product = Product(
                            id = "P4",
                            name = "Traditional Veshti",
                            description = "High-quality traditional veshti",
                            basePrice = 1299.0,
                            salePrice = 999.0,
                            categoryId = "2",
                            stockQuantity = 8,
                            isActive = true,
                            createdAt = fiveDaysAgo,
                            updatedAt = fiveDaysAgo
                        ),
                        quantity = 1,
                        price = 999.0
                    ),
                    OrderItem(
                        product = Product(
                            id = "P5",
                            name = "Cotton Shirt",
                            description = "Comfortable cotton shirt",
                            basePrice = 799.0,
                            salePrice = 599.0,
                            categoryId = "3",
                            stockQuantity = 20,
                            isActive = true,
                            createdAt = fiveDaysAgo,
                            updatedAt = fiveDaysAgo
                        ),
                        quantity = 2,
                        price = 599.0
                    )
                ),
                totalAmount = 2197.0,
                shippingAddress = "789 Pine St, Madurai, TN 625001",
                status = OrderStatus.DELIVERED,
                paymentMethod = "UPI",
                orderDate = fiveDaysAgo,
                deliveryDate = twoDaysAgo,
                trackingNumber = "TN-876543210"
            ),
            Order(
                id = "ORD-45678901",
                userId = "user123",
                items = listOf(
                    OrderItem(
                        product = Product(
                            id = "P6",
                            name = "Silk Saree",
                            description = "Premium silk saree",
                            basePrice = 5999.0,
                            salePrice = 4999.0,
                            categoryId = "1",
                            stockQuantity = 3,
                            isActive = true,
                            createdAt = tenDaysAgo,
                            updatedAt = tenDaysAgo
                        ),
                        quantity = 1,
                        price = 4999.0
                    )
                ),
                totalAmount = 4999.0,
                shippingAddress = "101 Maple St, Trichy, TN 620001",
                status = OrderStatus.CANCELLED,
                paymentMethod = "Net Banking",
                orderDate = tenDaysAgo,
                deliveryDate = null,
                trackingNumber = null
            ),
            Order(
                id = "ORD-56789012",
                userId = "user123",
                items = listOf(
                    OrderItem(
                        product = Product(
                            id = "P7",
                            name = "Designer Kurta",
                            description = "Designer kurta for special occasions",
                            basePrice = 1899.0,
                            salePrice = 1699.0,
                            categoryId = "4",
                            stockQuantity = 7,
                            isActive = true,
                            createdAt = twentyDaysAgo,
                            updatedAt = twentyDaysAgo
                        ),
                        quantity = 1,
                        price = 1699.0
                    ),
                    OrderItem(
                        product = Product(
                            id = "P8",
                            name = "Cotton Pants",
                            description = "Comfortable cotton pants",
                            basePrice = 1099.0,
                            salePrice = 899.0,
                            categoryId = "5",
                            stockQuantity = 12,
                            isActive = true,
                            createdAt = twentyDaysAgo,
                            updatedAt = twentyDaysAgo
                        ),
                        quantity = 1,
                        price = 899.0
                    )
                ),
                totalAmount = 2598.0,
                shippingAddress = "202 Cedar St, Salem, TN 636001",
                status = OrderStatus.DELIVERED,
                paymentMethod = "Credit Card",
                orderDate = twentyDaysAgo,
                deliveryDate = tenDaysAgo,
                trackingNumber = "TN-765432109"
            )
        )
    }
} 