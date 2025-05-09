package com.tiruvear.textiles.data.repositories

import android.util.Log
import com.tiruvear.textiles.TiruvearApp
import com.tiruvear.textiles.data.models.Cart
import com.tiruvear.textiles.data.models.CartItem
import com.tiruvear.textiles.data.models.CartItemEntity
import com.tiruvear.textiles.data.models.Product
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

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
    private val TAG = "CartRepositoryImpl"
    private val supabase = TiruvearApp.supabaseClient
    private val productRepository = ProductRepositoryImpl()
    
    override suspend fun getCart(userId: String): Result<Cart> = withContext(Dispatchers.IO) {
        try {
            // Try to find an existing cart for this user
            val cartResponse = supabase.postgrest["carts"]
                .select {
                    filter("user_id", FilterOperator.EQ, userId)
                    filter("is_active", FilterOperator.EQ, true)
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
            
            val carts = cartResponse.decodeList<Map<String, Any?>>()
            
            // If cart exists, return it
            if (carts.isNotEmpty()) {
                val cartData = carts.first()
                val cart = Cart(
                    id = cartData["id"] as String,
                    userId = cartData["user_id"] as String,
                    createdAt = Date((cartData["created_at"] as String).toLong()),
                    updatedAt = Date((cartData["updated_at"] as String).toLong()),
                    items = emptyList()
                )
                return@withContext Result.success(cart)
            } else {
                // Create a new cart for this user
                val cartId = UUID.randomUUID().toString()
                val now = Date()
                
                val cartData = buildJsonObject {
                    put("id", cartId)
                    put("user_id", userId)
                    put("created_at", now.time.toString())
                    put("updated_at", now.time.toString())
                    put("is_active", true)
                }
                
                supabase.postgrest["carts"]
                    .insert(cartData, returning = Returning.MINIMAL)
                
                val newCart = Cart(
                    id = cartId,
                    userId = userId,
                    createdAt = now,
                    updatedAt = now,
                    items = emptyList()
                )
                
                Result.success(newCart)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cart: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun addToCart(userId: String, productId: String, variantId: String?, quantity: Int): Result<CartItemEntity> = withContext(Dispatchers.IO) {
        try {
            // Get or create cart for this user
            val cartResult = getCart(userId)
            if (cartResult.isFailure) {
                return@withContext Result.failure(cartResult.exceptionOrNull()!!)
            }
            
            val cart = cartResult.getOrNull()!!
            val cartId = cart.id
            
            // Get product details
            val productResult = productRepository.getProductById(productId)
            if (productResult.isFailure) {
                return@withContext Result.failure(Exception("Product not found"))
            }
            
            val product = productResult.getOrNull()!!
            
            // Check if this product is already in the cart
            val cartItemResponse = supabase.postgrest["cart_items"]
                .select {
                    filter("cart_id", FilterOperator.EQ, cartId)
                    filter("product_id", FilterOperator.EQ, productId)
                    if (variantId != null) {
                        filter("variant_id", FilterOperator.EQ, variantId)
                    } else {
                        filter("variant_id", FilterOperator.IS, JsonNull)
                    }
                }
            
            val cartItems = cartItemResponse.decodeList<Map<String, Any?>>()
            
            if (cartItems.isNotEmpty()) {
                // Update existing cart item
                val existingItem = cartItems.first()
                val existingId = existingItem["id"] as String
                val existingQuantity = (existingItem["quantity"] as Number).toInt()
                val newQuantity = existingQuantity + quantity
                
                // Update in database
                val updateData = buildJsonObject {
                    put("quantity", newQuantity)
                    put("updated_at", Date().time.toString())
                }
                
                supabase.postgrest["cart_items"]
                    .update(updateData) {
                        filter("id", FilterOperator.EQ, existingId)
                    }
                
                // Create return entity
                val updatedItem = CartItemEntity(
                    id = existingId,
                    cartId = cartId,
                    productId = productId,
                    variantId = variantId,
                    quantity = newQuantity,
                    createdAt = Date((existingItem["created_at"] as String).toLong()),
                    updatedAt = Date(),
                    product = product,
                    variant = null
                )
                
                Result.success(updatedItem)
            } else {
                // Add new cart item
                val cartItemId = UUID.randomUUID().toString()
                val now = Date()
                
                val cartItemData = buildJsonObject {
                    put("id", cartItemId)
                    put("cart_id", cartId)
                    put("product_id", productId)
                    put("variant_id", variantId?.let { JsonPrimitive(it) } ?: JsonNull)
                    put("quantity", JsonPrimitive(quantity))
                    put("created_at", JsonPrimitive(now.time.toString()))
                    put("updated_at", JsonPrimitive(now.time.toString()))
                    put("price", JsonPrimitive(product.price.toString()))
                }
                
                supabase.postgrest["cart_items"]
                    .insert(cartItemData, returning = Returning.MINIMAL)
                
                // Create return entity
                val newItem = CartItemEntity(
                    id = cartItemId,
                    cartId = cartId,
                    productId = productId,
                    variantId = variantId,
                    quantity = quantity,
                    createdAt = now,
                    updatedAt = now,
                    product = product,
                    variant = null
                )
                
                Result.success(newItem)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateCartItem(cartItemId: String, quantity: Int): Result<CartItemEntity> = withContext(Dispatchers.IO) {
        try {
            // Get current cart item
            val cartItemResponse = supabase.postgrest["cart_items"]
                .select {
                    filter("id", FilterOperator.EQ, cartItemId)
                }
            
            val cartItems = cartItemResponse.decodeList<Map<String, Any?>>()
            
            if (cartItems.isEmpty()) {
                return@withContext Result.failure(Exception("Cart item not found"))
            }
            
            val existingItem = cartItems.first()
            val productId = existingItem["product_id"] as String
            val cartId = existingItem["cart_id"] as String
            val variantId = existingItem["variant_id"] as? String
            
            // Update quantity
            val updateData = buildJsonObject {
                put("quantity", quantity)
                put("updated_at", Date().time.toString())
            }
            
            supabase.postgrest["cart_items"]
                .update(updateData) {
                    filter("id", FilterOperator.EQ, cartItemId)
                }
            
            // Get product details
            val productResult = productRepository.getProductById(productId)
            if (productResult.isFailure) {
                return@withContext Result.failure(Exception("Product not found"))
            }
            
            val product = productResult.getOrNull()!!
            
            // Create return entity
            val updatedItem = CartItemEntity(
                id = cartItemId,
                cartId = cartId,
                productId = productId,
                variantId = variantId,
                quantity = quantity,
                createdAt = Date((existingItem["created_at"] as String).toLong()),
                updatedAt = Date(),
                product = product,
                variant = null
            )
            
            Result.success(updatedItem)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating cart item: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun removeFromCart(cartItemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete cart item
            supabase.postgrest["cart_items"]
                .delete {
                    filter("id", FilterOperator.EQ, cartItemId)
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from cart: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun clearCart(cartId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Delete all items in the cart
            supabase.postgrest["cart_items"]
                .delete {
                    filter("cart_id", FilterOperator.EQ, cartId)
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cart: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getCartItemCount(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Get the cart for this user
            val cartResult = getCart(userId)
            if (cartResult.isFailure) {
                return@withContext Result.failure(cartResult.exceptionOrNull()!!)
            }
            
            val cart = cartResult.getOrNull()!!
            
            // Count items in the cart
            val countResponse = supabase.postgrest["cart_items"]
                .select(count = Count.EXACT) {
                    filter("cart_id", FilterOperator.EQ, cart.id)
                }
            
            // Convert the Long count to Int and return it
            val count = (countResponse.count() ?: 0L).toInt()
            
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cart item count: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getAnonymousCart(deviceId: String): Result<Cart> = withContext(Dispatchers.IO) {
        try {
            // For anonymous users, we can use a user ID based on their device ID
            val anonymousUserId = "guest_user_${deviceId}"
            return@withContext getCart(anonymousUserId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting anonymous cart: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun mergeAnonymousCart(anonymousCartId: String, userId: String): Result<Cart> = withContext(Dispatchers.IO) {
        try {
            // Get the user's cart (or create one)
            val userCartResult = getCart(userId)
            if (userCartResult.isFailure) {
                return@withContext Result.failure(userCartResult.exceptionOrNull()!!)
            }
            
            val userCart = userCartResult.getOrNull()!!
            
            // Get items from anonymous cart
            val anonymousItemsResponse = supabase.postgrest["cart_items"]
                .select {
                    filter("cart_id", FilterOperator.EQ, anonymousCartId)
                }
            
            val anonymousItems = anonymousItemsResponse.decodeList<Map<String, Any?>>()
            
            // For each anonymous item, add to user cart
            for (item in anonymousItems) {
                val productId = item["product_id"] as String
                val variantId = item["variant_id"] as? String
                val quantity = (item["quantity"] as Number).toInt()
                
                // Add to user cart
                addToCart(userId, productId, variantId, quantity)
            }
            
            // Clear anonymous cart
            clearCart(anonymousCartId)
            
            return@withContext getCart(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error merging carts: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getCartItems(userId: String): Result<List<CartItem>> = withContext(Dispatchers.IO) {
        try {
            // Get the cart for this user
            val cartResult = getCart(userId)
            if (cartResult.isFailure) {
                return@withContext Result.failure(cartResult.exceptionOrNull()!!)
            }
            
            val cart = cartResult.getOrNull()!!
            
            // Get cart items
            val cartItemsResponse = supabase.postgrest["cart_items"]
                .select {
                    filter("cart_id", FilterOperator.EQ, cart.id)
                }
            
            val cartItemsData = cartItemsResponse.decodeList<Map<String, Any?>>()
            
            // Convert to cart items
            val cartItems = mutableListOf<CartItem>()
            
            for (itemData in cartItemsData) {
                val productId = itemData["product_id"] as String
                val productResult = productRepository.getProductById(productId)
                
                if (productResult.isSuccess) {
                    val product = productResult.getOrNull()!!
                    val price = itemData["price"]?.toString()?.toDoubleOrNull() ?: product.price
                    
                    cartItems.add(
                        CartItem(
                            id = itemData["id"] as String,
                            product = product,
                            quantity = (itemData["quantity"] as Number).toInt(),
                            price = price,
                            cartId = cart.id
                        )
                    )
                }
            }
            
            Result.success(cartItems)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cart items: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun addToCart(userId: String, productId: String, quantity: Int): Result<CartItem> = withContext(Dispatchers.IO) {
        try {
            // Call the other addToCart method without variant ID
            val result = addToCart(userId, productId, null, quantity)
            
            if (result.isSuccess) {
                val cartItemEntity = result.getOrNull()!!
                
                // Get product details
                val productResult = productRepository.getProductById(productId)
                if (productResult.isFailure) {
                    return@withContext Result.failure(Exception("Product not found"))
                }
                
                val product = productResult.getOrNull()!!
                
                // Convert to CartItem
                val cartItem = CartItem(
                    id = cartItemEntity.id,
                    product = product,
                    quantity = cartItemEntity.quantity,
                    price = product.price,
                    cartId = cartItemEntity.cartId
                )
                
                Result.success(cartItem)
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getCartTotal(userId: String): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val cartItemsResult = getCartItems(userId)
            
            if (cartItemsResult.isSuccess) {
                val items = cartItemsResult.getOrNull()!!
                val total = items.sumOf { it.price * it.quantity }
                Result.success(total)
            } else {
                Result.failure(cartItemsResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating cart total: ${e.message}", e)
            Result.failure(e)
        }
    }
} 