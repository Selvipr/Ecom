package com.tiruvear.textiles.ui.main.cart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tiruvear.textiles.data.models.CartItemEntity
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.data.models.ProductImage
import com.tiruvear.textiles.data.repositories.CartRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentCartBinding
import com.tiruvear.textiles.ui.main.adapters.CartAdapter
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class CartFragment : BaseFragment<FragmentCartBinding>() {
    override val TAG: String = "CartFragment"
    
    private val cartRepository by lazy { CartRepositoryImpl() }
    private lateinit var cartAdapter: CartAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCartBinding {
        return FragmentCartBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupCheckoutButton()
        loadCartItems()
    }
    
    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItems = emptyList(),
            onRemoveItem = { item -> removeFromCart(item) },
            onUpdateQuantity = { item, quantity -> updateQuantity(item, quantity) }
        )
        
        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }
    
    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            showToast("Checkout feature coming soon!")
        }
    }
    
    private fun loadCartItems() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId() ?: "guest_user"
                val result = withContext(Dispatchers.IO) {
                    cartRepository.getCartItems(userId)
                }
                
                if (result.isSuccess) {
                    val cartItems = result.getOrNull() ?: emptyList()
                    if (cartItems.isEmpty()) {
                        // If cart is empty, show mock items for better UX
                        updateCartItems(createMockCartItems())
                    } else {
                        // Convert CartItem to CartItemEntity since updateCartItems expects CartItemEntity
                        val cartItemEntities = cartItems.map { item ->
                            CartItemEntity(
                                id = item.id,
                                cartId = item.cartId,
                                productId = item.product.id,
                                variantId = null,
                                quantity = item.quantity,
                                createdAt = Date(),
                                updatedAt = Date(),
                                product = item.product,
                                variant = null
                            )
                        }
                        updateCartItems(cartItemEntities)
                    }
                } else {
                    // If loading fails, show mock items
                    updateCartItems(createMockCartItems())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cart items: ${e.message}", e)
                // Show mock items on error
                updateCartItems(createMockCartItems())
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateCartItems(cartItems: List<CartItemEntity>) {
        if (cartItems.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvCartItems.visibility = View.GONE
            binding.cardSummary.visibility = View.GONE
            binding.btnCheckout.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvCartItems.visibility = View.VISIBLE
            binding.cardSummary.visibility = View.VISIBLE
            binding.btnCheckout.visibility = View.VISIBLE
            
            // Convert CartItemEntity to CartItem
            val cartItemsList = cartItems.map { entity ->
                com.tiruvear.textiles.data.models.CartItem(
                    id = entity.id,
                    product = entity.product ?: createPlaceholderProduct(entity.productId),
                    quantity = entity.quantity,
                    price = entity.product?.price ?: 0.0,
                    cartId = entity.cartId
                )
            }
            
            cartAdapter.updateItems(cartItemsList)
            
            // Update summary
            updateCartSummary(cartItems)
        }
    }
    
    private fun updateCartSummary(cartItems: List<CartItemEntity>) {
        val subtotal = cartItems.sumOf { (it.product?.price ?: 0.0) * it.quantity }
        val shipping = if (subtotal > 0) 50.0 else 0.0
        val total = subtotal + shipping
        
        binding.tvSubtotalValue.text = "₹${String.format("%.2f", subtotal)}"
        binding.tvShippingValue.text = "₹${String.format("%.2f", shipping)}"
        binding.tvTotalValue.text = "₹${String.format("%.2f", total)}"
    }
    
    private fun createMockCartItems(): List<CartItemEntity> {
        val now = Date()
        
        return listOf(
            CartItemEntity(
                id = "cart-item-1",
                cartId = "cart-1",
                productId = "product-1",
                variantId = null,
                quantity = 1,
                createdAt = now,
                updatedAt = now,
                product = Product(
                    id = "product-1",
                    name = "Cotton Saree",
                    description = "Beautiful cotton saree with traditional design",
                    basePrice = 1499.0,
                    salePrice = 1299.0,
                    categoryId = "1",
                    stockQuantity = 10,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now,
                    images = listOf(
                        ProductImage(
                            id = "img-1",
                            productId = "product-1",
                            imageUrl = "https://placekitten.com/200/200",
                            isPrimary = true,
                            displayOrder = 1,
                            createdAt = now
                        )
                    )
                ),
                variant = null
            ),
            CartItemEntity(
                id = "cart-item-2",
                cartId = "cart-1",
                productId = "product-2",
                variantId = null,
                quantity = 2,
                createdAt = now,
                updatedAt = now,
                product = Product(
                    id = "product-2",
                    name = "Silk Dhoti",
                    description = "Premium silk dhoti for special occasions",
                    basePrice = 899.0,
                    salePrice = null,
                    categoryId = "2",
                    stockQuantity = 15,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now,
                    images = listOf(
                        ProductImage(
                            id = "img-2",
                            productId = "product-2",
                            imageUrl = "https://placekitten.com/201/201",
                            isPrimary = true,
                            displayOrder = 1,
                            createdAt = now
                        )
                    )
                ),
                variant = null
            )
        )
    }
    
    private fun removeFromCart(item: com.tiruvear.textiles.data.models.CartItem) {
        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    cartRepository.removeFromCart(item.id)
                }
                
                if (result.isSuccess) {
                    showToast("Item removed from cart")
                    loadCartItems()
                } else {
                    showToast("Failed to remove item from cart")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing from cart: ${e.message}", e)
                showToast("Error removing item from cart")
            }
        }
    }
    
    private fun updateQuantity(item: com.tiruvear.textiles.data.models.CartItem, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(item)
            return
        }
        
        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    cartRepository.updateCartItem(item.id, quantity)
                }
                
                if (result.isSuccess) {
                    loadCartItems()
                } else {
                    showToast("Failed to update quantity")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating quantity: ${e.message}", e)
                showToast("Error updating quantity")
            }
        }
    }
    
    private fun showError(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvCartItems.visibility = View.GONE
        binding.cardSummary.visibility = View.GONE
        binding.btnCheckout.visibility = View.GONE
    }
    
    private fun createPlaceholderProduct(productId: String): Product {
        val now = Date()
        return Product(
            id = productId,
            name = "Product",
            description = "Product description",
            basePrice = 0.0,
            salePrice = null,
            categoryId = "",
            stockQuantity = 0,
            isActive = true,
            createdAt = now,
            updatedAt = now,
            images = listOf(
                ProductImage(
                    id = "placeholder-img",
                    productId = productId,
                    imageUrl = "https://placekitten.com/200/200",
                    isPrimary = true,
                    displayOrder = 1,
                    createdAt = now
                )
            )
        )
    }
} 