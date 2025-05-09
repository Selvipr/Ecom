package com.tiruvear.textiles.data.repositories

import android.util.Log
import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Order
import com.tiruvear.textiles.data.models.OrderItem
import com.tiruvear.textiles.data.models.OrderStatus
import com.tiruvear.textiles.data.models.Product
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import io.github.jan.supabase.postgrest.query.Order as QueryOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.postgrest.query.Returning

class OrderRepositoryImpl : OrderRepository {
    
    private val TAG = "OrderRepositoryImpl"
    private val supabase = TiruvearApp.supabaseClient
    private val productRepository = ProductRepositoryImpl()
    private val cartRepository = CartRepositoryImpl()
    
    override suspend fun getOrderById(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            // Get the order from Supabase
            val orderResponse = supabase.postgrest["orders"]
                .select {
                    filter("id", FilterOperator.EQ, orderId)
                }
            
            val orders = orderResponse.decodeList<Map<String, Any?>>()
            
            if (orders.isEmpty()) {
                return@withContext Result.failure(Exception("Order not found"))
            }
            
            val orderData = orders.first()
            
            // Get order items
            val orderItemsResponse = supabase.postgrest["order_items"]
                .select {
                    filter("order_id", FilterOperator.EQ, orderId)
                }
            
            val orderItemsData = orderItemsResponse.decodeList<Map<String, Any?>>()
            
            val orderItems = mutableListOf<OrderItem>()
            
            for (itemData in orderItemsData) {
                val productId = itemData["product_id"] as String
                val productResult = productRepository.getProductById(productId)
                
                if (productResult.isSuccess) {
                    val product = productResult.getOrNull()!!
                    val price = (itemData["price"] as Number).toDouble()
                    val quantity = (itemData["quantity"] as Number).toInt()
                    
                    orderItems.add(
                        OrderItem(
                            product = product,
                            quantity = quantity,
                            price = price
                        )
                    )
                }
            }
            
            // Get address information
            val addressId = orderData["address_id"] as String
            val addressResponse = supabase.postgrest["addresses"]
                .select {
                    filter("id", FilterOperator.EQ, addressId)
                }
            
            val addresses = addressResponse.decodeList<Map<String, Any?>>()
            val addressData = if (addresses.isNotEmpty()) addresses.first() else null
            
            val shippingAddress = if (addressData != null) {
                "${addressData["name"]}, ${addressData["address_line1"]}, " +
                "${addressData["address_line2"] ?: ""}, ${addressData["city"]}, " +
                "${addressData["state"]} ${addressData["postal_code"]}, ${addressData["country"]}"
            } else {
                "Address not found"
            }
            
            // Create order object
            val order = Order(
                id = orderId,
                userId = orderData["user_id"] as String,
                items = orderItems,
                totalAmount = (orderData["total_amount"] as Number).toDouble(),
                shippingAddress = shippingAddress,
                status = OrderStatus.valueOf((orderData["order_status"] as String).uppercase()),
                paymentMethod = orderData["payment_method"] as String,
                orderDate = Date((orderData["created_at"] as String).toLong()),
                deliveryDate = null, // Not stored in our DB schema
                trackingNumber = null // Not stored in our DB schema
            )
            
            Result.success(order)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting order: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getOrdersByUser(userId: String): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            // Get orders from Supabase
            val orderResponse = supabase.postgrest["orders"]
                .select {
                    filter("user_id", FilterOperator.EQ, userId)
                    order("created_at", QueryOrder.DESCENDING)
                }
            
            val ordersData = orderResponse.decodeList<Map<String, Any?>>()
            val orders = mutableListOf<Order>()
            
            for (orderData in ordersData) {
                val orderId = orderData["id"] as String
                
                // Get order items
                val orderItemsResponse = supabase.postgrest["order_items"]
                    .select {
                        filter("order_id", FilterOperator.EQ, orderId)
                    }
                
                val orderItemsData = orderItemsResponse.decodeList<Map<String, Any?>>()
                
                val orderItems = mutableListOf<OrderItem>()
                
                for (itemData in orderItemsData) {
                    val productId = itemData["product_id"] as String
                    val productResult = productRepository.getProductById(productId)
                    
                    if (productResult.isSuccess) {
                        val product = productResult.getOrNull()!!
                        val price = (itemData["price"] as Number).toDouble()
                        val quantity = (itemData["quantity"] as Number).toInt()
                        
                        orderItems.add(
                            OrderItem(
                                product = product,
                                quantity = quantity,
                                price = price
                            )
                        )
                    }
                }
                
                // Get address information
                val addressId = orderData["address_id"] as String
                val addressResponse = supabase.postgrest["addresses"]
                    .select {
                        filter("id", FilterOperator.EQ, addressId)
                    }
                
                val addresses = addressResponse.decodeList<Map<String, Any?>>()
                val addressData = if (addresses.isNotEmpty()) addresses.first() else null
                
                val shippingAddress = if (addressData != null) {
                    "${addressData["name"]}, ${addressData["address_line1"]}, " +
                    "${addressData["address_line2"] ?: ""}, ${addressData["city"]}, " +
                    "${addressData["state"]} ${addressData["postal_code"]}, ${addressData["country"]}"
                } else {
                    "Address not found"
                }
                
                // Create order object
                val order = Order(
                    id = orderId,
                    userId = userId,
                    items = orderItems,
                    totalAmount = (orderData["total_amount"] as Number).toDouble(),
                    shippingAddress = shippingAddress,
                    status = OrderStatus.valueOf((orderData["order_status"] as String).uppercase()),
                    paymentMethod = orderData["payment_method"] as String,
                    orderDate = Date((orderData["created_at"] as String).toLong()),
                    deliveryDate = null, // Not stored in our DB schema
                    trackingNumber = null // Not stored in our DB schema
                )
                
                orders.add(order)
            }
            
            Result.success(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting orders by user: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            // Update order status in Supabase
            val updateData = buildJsonObject {
                put("order_status", status.lowercase())
                put("updated_at", Date().time.toString())
            }
            
            supabase.postgrest["orders"]
                .update(updateData) {
                    filter("id", FilterOperator.EQ, orderId)
                }
            
            // Get the updated order
            return@withContext getOrderById(orderId)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun cancelOrder(orderId: String): Result<Order> = withContext(Dispatchers.IO) {
        try {
            // Update order status to cancelled in Supabase
            val updateData = buildJsonObject {
                put("order_status", "cancelled")
                put("updated_at", Date().time.toString())
            }
            
            supabase.postgrest["orders"]
                .update(updateData) {
                    filter("id", FilterOperator.EQ, orderId)
                }
            
            // Get the updated order
            return@withContext getOrderById(orderId)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling order: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getUserOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val userId = TiruvearApp.getCurrentUserId() ?: return@withContext Result.failure(Exception("User not logged in"))
            return@withContext getOrdersByUser(userId)
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
            // Get cart items
            val cartItemsResult = cartRepository.getCartItems(userId)
            if (cartItemsResult.isFailure) {
                return@withContext Result.failure(Exception("Failed to get cart items"))
            }
            
            val cartItems = cartItemsResult.getOrNull()!!
            
            if (cartItems.isEmpty()) {
                return@withContext Result.failure(Exception("Cart is empty"))
            }
            
            // Calculate subtotal
            val subtotal = cartItems.sumOf { it.price * it.quantity }
            
            // Calculate total amount
            val totalAmount = subtotal + shippingCharge - discountAmount
            
            // Create order in Supabase
            val orderId = UUID.randomUUID().toString()
            val now = Date()
            
            val orderData = buildJsonObject {
                put("id", orderId)
                put("user_id", userId)
                put("address_id", addressId)
                put("payment_method", paymentMethod)
                put("payment_status", "pending")
                put("order_status", "placed")
                put("subtotal", subtotal.toString())
                put("shipping_charge", shippingCharge.toString())
                put("discount_amount", discountAmount.toString())
                put("tax_amount", "0")
                put("total_amount", totalAmount.toString())
                put("created_at", now.time.toString())
                put("updated_at", now.time.toString())
            }
            
            supabase.postgrest["orders"]
                .insert(orderData, returning = Returning.MINIMAL)
            
            // Create order items
            for (item in cartItems) {
                val orderItemId = UUID.randomUUID().toString()
                val orderItemData = buildJsonObject {
                    put("id", orderItemId)
                    put("order_id", orderId)
                    put("product_id", item.product.id)
                    put("variant_id", null)
                    put("quantity", item.quantity)
                    put("price", item.price.toString())
                    put("created_at", now.time.toString())
                }
                
                supabase.postgrest["order_items"]
                    .insert(orderItemData, returning = Returning.MINIMAL)
            }
            
            // Clear the cart after order creation
            cartRepository.clearCart(cartId)
            
            // Get the created order
            return@withContext getOrderById(orderId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order: ${e.message}", e)
            Result.failure(e)
        }
    }
} 