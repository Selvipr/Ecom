package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Cart
import com.tiruvear.textiles.data.models.CartItemEntity
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.FilterOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Returning
import com.tiruvear.textiles.data.models.CartItem
import com.tiruvear.textiles.data.models.Product

interface CartRepository {
    suspend fun getCart(userId: String): Result<Cart>
    suspend fun addToCart(userId: String, productId: String, variantId: String?, quantity: Int): Result<CartItemEntity>
    suspend fun updateCartItem(cartItemId: String, quantity: Int): Result<CartItemEntity>
    suspend fun removeFromCart(cartItemId: String): Result<Unit>
    suspend fun clearCart(cartId: String): Result<Unit>
    suspend fun getCartItemCount(userId: String): Result<Int>
    suspend fun getAnonymousCart(deviceId: String): Result<Cart>
    suspend fun mergeAnonymousCart(anonymousCartId: String, userId: String): Result<Cart>
    suspend fun getCartItems(userId: String): Result<List<CartItem>>
    suspend fun addToCart(userId: String, productId: String, quantity: Int): Result<CartItem>
    suspend fun getCartTotal(userId: String): Result<Double>
}

class CartRepositoryImpl : CartRepository {
    
    private val supabase = TiruvearApp.supabaseClient
    private val productRepository = ProductRepositoryImpl()
    
    override suspend fun getCart(userId: String): Result<Cart> = withContext(Dispatchers.IO) {
        try {
            // Check if user has an existing cart
            val cartResponse = supabase.postgrest["carts"]
                .select {
                    filter("user_id", FilterOperator.EQ, userId)
                }
                .decodeSingle<Map<String, Any>>()
            
            val cartId = cartResponse["id"] as String
            
            // Get cart items
            val cartItems = supabase.postgrest["cart_items"]
                .select {
                    filter("cart_id", FilterOperator.EQ, cartId)
                }
                .decodeList<Map<String, Any>>()
                .map { item -> mapToCartItem(item) }
            
            val cart = Cart(
                id = cartId,
                userId = userId,
                createdAt = cartResponse["created_at"] as Date,
                updatedAt = cartResponse["updated_at"] as Date,
                items = cartItems
            )
            
            Result.success(cart)
        } catch (e: Exception) {
            // If cart doesn't exist, create a new one
            try {
                val newCartId = UUID.randomUUID().toString()
                val now = Date()
                
                supabase.postgrest["carts"].insert(
                    object {
                        val id = newCartId
                        val user_id = userId
                        val created_at = now
                        val updated_at = now
                    }
                )
                
                val cart = Cart(
                    id = newCartId,
                    userId = userId,
                    createdAt = now,
                    updatedAt = now,
                    items = emptyList()
                )
                
                Result.success(cart)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }
    
    override suspend fun addToCart(userId: String, productId: String, variantId: String?, quantity: Int): Result<CartItemEntity> = withContext(Dispatchers.IO) {
        try {
            // Get or create cart
            val cartResult = getCart(userId)
            if (cartResult.isFailure) {
                return@withContext Result.failure(cartResult.exceptionOrNull()!!)
            }
            
            val cart = cartResult.getOrNull()!!
            
            // Check if item already exists in cart
            val existingItems = if (variantId != null) {
                supabase.postgrest["cart_items"]
                    .select {
                        filter("cart_id", FilterOperator.EQ, cart.id)
                        filter("product_id", FilterOperator.EQ, productId)
                        filter("variant_id", FilterOperator.EQ, variantId)
                    }
                    .decodeList<Map<String, Any>>()
            } else {
                supabase.postgrest["cart_items"]
                    .select {
                        filter("cart_id", FilterOperator.EQ, cart.id)
                        filter("product_id", FilterOperator.EQ, productId)
                        filter("variant_id", FilterOperator.IS, JsonNull)
                    }
                    .decodeList<Map<String, Any>>()
            }
            
            if (existingItems.isNotEmpty()) {
                // Update quantity
                val existingItem = existingItems.first()
                val existingId = existingItem["id"] as String
                val existingQuantity = (existingItem["quantity"] as Number).toInt()
                
                return@withContext updateCartItem(existingId, existingQuantity + quantity)
            }
            
            // Add new item
            val cartItemId = UUID.randomUUID().toString()
            val now = Date()
            
            if (variantId != null) {
                supabase.postgrest["cart_items"].insert(
                    object {
                        val id = cartItemId
                        val cart_id = cart.id
                        val product_id = productId
                        val variant_id = variantId
                        val quantity = quantity
                        val created_at = now
                        val updated_at = now
                    }
                )
            } else {
                supabase.postgrest["cart_items"].insert(
                    object {
                        val id = cartItemId
                        val cart_id = cart.id
                        val product_id = productId
                        val quantity = quantity
                        val created_at = now
                        val updated_at = now
                    }
                )
            }
            
            // Fetch the added item with product details
            val cartItemData = supabase.postgrest["cart_items"]
                .select {
                    filter("id", FilterOperator.EQ, cartItemId)
                }
                .decodeSingle<Map<String, Any>>()
            
            Result.success(mapToCartItem(cartItemData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateCartItem(cartItemId: String, quantity: Int): Result<CartItemEntity> = withContext(Dispatchers.IO) {
        try {
            val now = Date()
            
            // Create JSON object for update
            val updateData = buildJsonObject {
                put("quantity", quantity)
                put("updated_at", now.toString())
            }
            
            supabase.postgrest["cart_items"]
                .update(updateData) {
                    filter("id", FilterOperator.EQ, cartItemId)
                }
            
            // Fetch the updated item with product details
            val cartItemData = supabase.postgrest["cart_items"]
                .select {
                    filter("id", FilterOperator.EQ, cartItemId)
                }
                .decodeSingle<Map<String, Any>>()
            
            Result.success(mapToCartItem(cartItemData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeFromCart(cartItemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["cart_items"]
                .delete {
                    filter("id", FilterOperator.EQ, cartItemId)
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearCart(cartId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["cart_items"]
                .delete {
                    filter("cart_id", FilterOperator.EQ, cartId)
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCartItemCount(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Get cart ID
            val cartResponse = supabase.postgrest["carts"]
                .select {
                    filter("user_id", FilterOperator.EQ, userId)
                }
                .decodeSingle<Map<String, Any>>()
            
            val cartId = cartResponse["id"] as String
            
            // Get count of items
            val countResponse = supabase.postgrest["cart_items"]
                .select(count = Count.EXACT) {
                    filter("cart_id", FilterOperator.EQ, cartId)
                }
                .decodeSingle<Map<String, Any>>()
            
            val count = countResponse["count"] as Int
            
            Result.success(count)
        } catch (e: Exception) {
            Result.success(0) // No cart or empty cart
        }
    }
    
    override suspend fun getAnonymousCart(deviceId: String): Result<Cart> = withContext(Dispatchers.IO) {
        try {
            // Similar to getCart, but using device_id instead of user_id
            val cartResponse = supabase.postgrest["carts"]
                .select {
                    filter("device_id", FilterOperator.EQ, deviceId)
                    filter("user_id", FilterOperator.IS, JsonNull)
                }
                .decodeSingle<Map<String, Any>>()
            
            val cartId = cartResponse["id"] as String
            
            // Get cart items
            val cartItems = supabase.postgrest["cart_items"]
                .select {
                    filter("cart_id", FilterOperator.EQ, cartId)
                }
                .decodeList<Map<String, Any>>()
                .map { item -> mapToCartItem(item) }
            
            val cart = Cart(
                id = cartId,
                userId = "", // Empty for anonymous cart
                createdAt = cartResponse["created_at"] as Date,
                updatedAt = cartResponse["updated_at"] as Date,
                items = cartItems
            )
            
            Result.success(cart)
        } catch (e: Exception) {
            // If cart doesn't exist, create a new one
            try {
                val newCartId = UUID.randomUUID().toString()
                val now = Date()
                
                supabase.postgrest["carts"].insert(
                    object {
                        val id = newCartId
                        val device_id = deviceId
                        val created_at = now
                        val updated_at = now
                    }
                )
                
                val cart = Cart(
                    id = newCartId,
                    userId = "",
                    createdAt = now,
                    updatedAt = now,
                    items = emptyList()
                )
                
                Result.success(cart)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }
    
    override suspend fun mergeAnonymousCart(anonymousCartId: String, userId: String): Result<Cart> = withContext(Dispatchers.IO) {
        try {
            // Get user's cart
            val userCartResult = getCart(userId)
            if (userCartResult.isFailure) {
                return@withContext Result.failure(userCartResult.exceptionOrNull()!!)
            }
            
            val userCart = userCartResult.getOrNull()!!
            
            // Get anonymous cart items
            val anonymousItems = supabase.postgrest["cart_items"]
                .select {
                    filter("cart_id", FilterOperator.EQ, anonymousCartId)
                }
                .decodeList<Map<String, Any>>()
            
            // Merge items
            for (item in anonymousItems) {
                val productId = item["product_id"] as String
                val variantId = item["variant_id"] as? String
                val quantity = (item["quantity"] as Number).toInt()
                
                addToCart(userId, productId, variantId, quantity)
            }
            
            // Delete anonymous cart
            supabase.postgrest["carts"]
                .delete {
                    filter("id", FilterOperator.EQ, anonymousCartId)
                }
            
            // Get updated cart
            return@withContext getCart(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCartItems(userId: String): Result<List<CartItem>> = withContext(Dispatchers.IO) {
        try {
            // Get cart ID or create a new one
            val cartId = getOrCreateCartId(userId)
            
            // Get cart items
            val cartItems = supabase.postgrest["cart_items"]
                .select {
                    filter("cart_id", FilterOperator.EQ, cartId)
                }
                .decodeList<Map<String, Any>>()
            
            // Map to cart items with product details
            val cartItemsWithProducts = cartItems.map { cartItemData ->
                val productId = cartItemData["product_id"] as String
                val productResult = productRepository.getProductById(productId)
                val product = productResult.getOrNull()
                
                CartItem(
                    id = cartItemData["id"] as String,
                    product = product ?: createPlaceholderProduct(productId),
                    quantity = (cartItemData["quantity"] as Number).toInt(),
                    price = (cartItemData["price"] as Number).toDouble(),
                    cartId = cartId
                )
            }
            
            Result.success(cartItemsWithProducts)
        } catch (e: Exception) {
            // Return empty list on error to avoid crashes
            Result.success(emptyList())
        }
    }
    
    override suspend fun addToCart(userId: String, productId: String, quantity: Int): Result<CartItem> = withContext(Dispatchers.IO) {
        try {
            // Get cart ID or create a new one
            val cartId = getOrCreateCartId(userId)
            
            // Check if product already exists in cart
            val existingCartItems = supabase.postgrest["cart_items"]
                .select {
                    filter("cart_id", FilterOperator.EQ, cartId)
                    filter("product_id", FilterOperator.EQ, productId)
                }
                .decodeList<Map<String, Any>>()
            
            val cartItemId: String
            val finalQuantity: Int
            
            if (existingCartItems.isNotEmpty()) {
                // Update existing cart item
                val existingItem = existingCartItems.first()
                cartItemId = existingItem["id"] as String
                finalQuantity = (existingItem["quantity"] as Number).toInt() + quantity
                
                supabase.postgrest["cart_items"]
                    .update(mapOf("quantity" to finalQuantity)) {
                        filter("id", FilterOperator.EQ, cartItemId)
                    }
            } else {
                // Get product details
                val productResult = productRepository.getProductById(productId)
                val product = productResult.getOrNull() 
                    ?: return@withContext Result.failure(Exception("Product not found"))
                
                // Create new cart item
                cartItemId = UUID.randomUUID().toString()
                finalQuantity = quantity
                
                val cartItemData = mapOf(
                    "id" to cartItemId,
                    "cart_id" to cartId,
                    "product_id" to productId,
                    "quantity" to quantity,
                    "price" to product.price,
                    "created_at" to Date(),
                    "updated_at" to Date()
                )
                
                supabase.postgrest["cart_items"].insert(cartItemData)
            }
            
            // Return updated cart item
            val productResult = productRepository.getProductById(productId)
            val product = productResult.getOrNull() 
                ?: return@withContext Result.failure(Exception("Product not found"))
            
            Result.success(
                CartItem(
                    id = cartItemId,
                    product = product,
                    quantity = finalQuantity,
                    price = product.price,
                    cartId = cartId
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCartTotal(userId: String): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val cartItemsResult = getCartItems(userId)
            
            if (cartItemsResult.isSuccess) {
                val cartItems = cartItemsResult.getOrNull() ?: emptyList()
                val total = cartItems.sumOf { it.quantity * it.price }
                Result.success(total)
            } else {
                Result.failure(cartItemsResult.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper methods
    private suspend fun getOrCreateCartId(userId: String): String {
        val existingCartId = getCartId(userId)
        if (existingCartId != null) {
            return existingCartId
        }
        
        // Create new cart
        val cartId = UUID.randomUUID().toString()
        val cartData = mapOf(
            "id" to cartId,
            "user_id" to userId,
            "created_at" to Date(),
            "updated_at" to Date()
        )
        
        supabase.postgrest["carts"].insert(cartData)
        return cartId
    }
    
    private suspend fun getCartId(userId: String): String? {
        return try {
            val carts = supabase.postgrest["carts"]
                .select {
                    filter("user_id", FilterOperator.EQ, userId)
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeList<Map<String, Any>>()
            
            if (carts.isNotEmpty()) {
                carts.first()["id"] as String
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createPlaceholderProduct(productId: String): Product {
        return Product(
            id = productId,
            name = "Product Not Found",
            description = "This product is no longer available",
            basePrice = 0.0,
            salePrice = null,
            categoryId = "",
            stockQuantity = 0,
            isActive = false,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun mapToCartItem(data: Map<String, Any>): CartItemEntity {
        return CartItemEntity(
            id = data["id"] as String,
            cartId = data["cart_id"] as String,
            productId = data["product_id"] as String,
            variantId = data["variant_id"] as? String,
            quantity = (data["quantity"] as Number).toInt(),
            createdAt = data["created_at"] as Date,
            updatedAt = data["updated_at"] as Date,
            product = null, // Product relationship needs to be fetched separately
            variant = null  // Variant relationship needs to be fetched separately
        )
    }
} 