package com.tiruvear.textiles.data.repositories

import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Cart
import com.tiruvear.textiles.data.models.CartItem
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

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
            val cartResponse = supabase.from("carts")
                .select()
                .eq("user_id", userId)
                .single()
                .decodeAs<Map<String, Any>>()
            
            val cartId = cartResponse["id"] as String
            
            // Get cart items
            val cartItems = supabase.from("cart_items")
                .select("*, products(*), product_variants(*)")
                .eq("cart_id", cartId)
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
                
                supabase.from("carts").insert(
                    values = mapOf(
                        "id" to newCartId,
                        "user_id" to userId,
                        "created_at" to now,
                        "updated_at" to now
                    )
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
            val existingItems = supabase.from("cart_items")
                .select()
                .eq("cart_id", cart.id)
                .eq("product_id", productId)
                .also { query -> 
                    if (variantId != null) {
                        query.eq("variant_id", variantId)
                    } else {
                        query.isNull("variant_id")
                    }
                }
                .decodeList<Map<String, Any>>()
            
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
            
            val values = mutableMapOf(
                "id" to cartItemId,
                "cart_id" to cart.id,
                "product_id" to productId,
                "quantity" to quantity,
                "created_at" to now,
                "updated_at" to now
            )
            
            if (variantId != null) {
                values["variant_id"] = variantId
            }
            
            supabase.from("cart_items").insert(values = values)
            
            // Fetch the added item with product details
            val cartItemData = supabase.from("cart_items")
                .select("*, products(*), product_variants(*)")
                .eq("id", cartItemId)
                .single()
                .decodeAs<Map<String, Any>>()
            
            Result.success(mapToCartItem(cartItemData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateCartItem(cartItemId: String, quantity: Int): Result<CartItem> = withContext(Dispatchers.IO) {
        try {
            val now = Date()
            
            supabase.from("cart_items").update(
                values = mapOf(
                    "quantity" to quantity,
                    "updated_at" to now
                )
            ) {
                eq("id", cartItemId)
            }
            
            // Fetch the updated item with product details
            val cartItemData = supabase.from("cart_items")
                .select("*, products(*), product_variants(*)")
                .eq("id", cartItemId)
                .single()
                .decodeAs<Map<String, Any>>()
            
            Result.success(mapToCartItem(cartItemData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeFromCart(cartItemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("cart_items").delete {
                eq("id", cartItemId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearCart(cartId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("cart_items").delete {
                eq("cart_id", cartId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCartItemCount(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Get cart ID
            val cartResponse = supabase.from("carts")
                .select()
                .eq("user_id", userId)
                .single()
                .decodeAs<Map<String, Any>>()
            
            val cartId = cartResponse["id"] as String
            
            // Get count of items
            val countResponse = supabase.from("cart_items")
                .select(count = true)
                .eq("cart_id", cartId)
                .decodeAs<Map<String, Any>>()
            
            val count = countResponse["count"] as Int
            
            Result.success(count)
        } catch (e: Exception) {
            Result.success(0) // No cart or empty cart
        }
    }
    
    override suspend fun getAnonymousCart(deviceId: String): Result<Cart> = withContext(Dispatchers.IO) {
        try {
            // Similar to getCart, but using device_id instead of user_id
            val cartResponse = supabase.from("carts")
                .select()
                .eq("device_id", deviceId)
                .isNull("user_id")
                .single()
                .decodeAs<Map<String, Any>>()
            
            val cartId = cartResponse["id"] as String
            
            // Get cart items
            val cartItems = supabase.from("cart_items")
                .select("*, products(*), product_variants(*)")
                .eq("cart_id", cartId)
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
                
                supabase.from("carts").insert(
                    values = mapOf(
                        "id" to newCartId,
                        "device_id" to deviceId,
                        "created_at" to now,
                        "updated_at" to now
                    )
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
            val anonymousItems = supabase.from("cart_items")
                .select("*")
                .eq("cart_id", anonymousCartId)
                .decodeList<Map<String, Any>>()
            
            // Merge items
            for (item in anonymousItems) {
                val productId = item["product_id"] as String
                val variantId = item["variant_id"] as? String
                val quantity = (item["quantity"] as Number).toInt()
                
                addToCart(userId, productId, variantId, quantity)
            }
            
            // Delete anonymous cart
            supabase.from("carts").delete {
                eq("id", anonymousCartId)
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