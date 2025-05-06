package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Cart
import com.tiruvear.textiles.data.models.CartItem
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

interface CartRepository {
    suspend fun getCart(userId: String): Result<Cart>
    suspend fun addToCart(userId: String, productId: String, variantId: String?, quantity: Int): Result<CartItem>
    suspend fun updateCartItem(cartItemId: String, quantity: Int): Result<CartItem>
    suspend fun removeFromCart(cartItemId: String): Result<Unit>
    suspend fun clearCart(cartId: String): Result<Unit>
    suspend fun getCartItemCount(userId: String): Result<Int>
    suspend fun getAnonymousCart(deviceId: String): Result<Cart>
    suspend fun mergeAnonymousCart(anonymousCartId: String, userId: String): Result<Cart>
}

class CartRepositoryImpl : CartRepository {
    
    private val supabase = TiruvearApp.supabaseClient
    
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
    
    override suspend fun addToCart(userId: String, productId: String, variantId: String?, quantity: Int): Result<CartItem> = withContext(Dispatchers.IO) {
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
    
    override suspend fun updateCartItem(cartItemId: String, quantity: Int): Result<CartItem> = withContext(Dispatchers.IO) {
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
    
    @Suppress("UNCHECKED_CAST")
    private fun mapToCartItem(data: Map<String, Any>): CartItem {
        return CartItem(
            id = data["id"] as String,
            cartId = data["cart_id"] as String,
            productId = data["product_id"] as String,
            variantId = data["variant_id"] as? String,
            quantity = (data["quantity"] as Number).toInt(),
            createdAt = data["created_at"] as Date,
            updatedAt = data["updated_at"] as Date
            // Note: Product and variant relationships need more complex mapping
        )
    }
} 