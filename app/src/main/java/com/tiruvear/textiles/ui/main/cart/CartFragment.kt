package com.tiruvear.textiles.ui.main.cart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.CartItem
import com.tiruvear.textiles.data.models.CartItemEntity
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.data.models.ProductImage
import com.tiruvear.textiles.data.repositories.CartRepositoryImpl
import com.tiruvear.textiles.data.repositories.OrderRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentCartBinding
import com.tiruvear.textiles.ui.main.adapters.CartAdapter
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class CartFragment : BaseFragment<FragmentCartBinding>() {
    override val TAG: String = "CartFragment"
    
    private val cartRepository by lazy { CartRepositoryImpl() }
    private val orderRepository by lazy { OrderRepositoryImpl() }
    private lateinit var cartAdapter: CartAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // Track cart state
    private var cartItems = listOf<CartItem>()
    private var cartTotal = 0.0
    private var shippingCost = 50.0
    private var discount = 0.0
    private var appliedCoupon: String? = null
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCartBinding {
        return FragmentCartBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupCheckoutButton()
        setupRefreshLayout()
        setupCouponSection()
        loadCartItems()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload cart items when returning to this fragment
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
    
    private fun setupRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadCartItems()
        }
    }
    
    private fun setupCouponSection() {
        binding.btnApplyCoupon.setOnClickListener {
            val couponCode = binding.etCouponCode.text.toString().trim()
            
            if (couponCode.isEmpty()) {
                showToast("Please enter a coupon code")
                return@setOnClickListener
            }
            
            // Apply mock coupon logic
            when (couponCode.uppercase()) {
                "WELCOME10" -> {
                    discount = cartTotal * 0.1 // 10% discount
                    appliedCoupon = couponCode
                    showToast("Coupon applied: 10% discount")
                    updateCartSummary()
                }
                "TIRUVEAR20" -> {
                    discount = cartTotal * 0.2 // 20% discount
                    appliedCoupon = couponCode
                    showToast("Coupon applied: 20% discount")
                    updateCartSummary()
                }
                "FREESHIP" -> {
                    shippingCost = 0.0
                    appliedCoupon = couponCode
                    showToast("Coupon applied: Free shipping")
                    updateCartSummary()
                }
                else -> {
                    showToast("Invalid coupon code")
                }
            }
        }
    }
    
    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) {
                showToast("Your cart is empty")
                return@setOnClickListener
            }
            
            // Show checkout confirmation dialog
            showCheckoutConfirmationDialog()
        }
    }
    
    private fun showCheckoutConfirmationDialog() {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Order")
            .setMessage("Total amount: ₹${String.format("%.2f", cartTotal + shippingCost - discount)}\n\nProceed to checkout?")
            .setPositiveButton("Proceed") { _, _ ->
                proceedToCheckout()
            }
            .setNegativeButton("Cancel", null)
        
        dialogBuilder.show()
    }
    
    private fun proceedToCheckout() {
        // Show processing dialog
        showLoading(true, "Processing your order...")
        
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId() ?: "guest_user"
                
                // For simplicity, we'll use a dummy address ID and cart ID
                val addressId = "addr_" + UUID.randomUUID().toString().substring(0, 8)
                val cartId = "cart_" + UUID.randomUUID().toString().substring(0, 8)
                
                // Create the order
                val result = withContext(Dispatchers.IO) {
                    orderRepository.createOrder(
                        userId = userId,
                        addressId = addressId,
                        cartId = cartId,
                        paymentMethod = "Cash on Delivery", // Default payment method
                        shippingCharge = shippingCost,
                        discountAmount = discount
                    )
                }
                
                if (result.isSuccess) {
                    val order = result.getOrNull()
                    
                    // Clear the cart after successful order
                    withContext(Dispatchers.IO) {
                        if (cartItems.isNotEmpty()) {
                            cartRepository.clearCart(cartItems.first().cartId)
                        }
                    }
                    
                    // Navigate to order confirmation
                    val bundle = Bundle().apply {
                        putString("orderId", order?.id)
                    }
                    
                    findNavController().navigate(
                        R.id.action_navigation_cart_to_orderConfirmationFragment,
                        bundle
                    )
                    
                    showToast("Order placed successfully!")
                } else {
                    Log.e(TAG, "Error creating order: ${result.exceptionOrNull()?.message}")
                    showToast("Failed to place order. Please try again.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during checkout: ${e.message}", e)
                showToast("An error occurred. Please try again later.")
            } finally {
                showLoading(false)
            }
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
                    val items = result.getOrNull() ?: emptyList()
                    if (items.isEmpty()) {
                        // If cart is empty, show mock items for better UX
                        updateCartItems(createMockCartItems())
                    } else {
                        // Convert CartItem to CartItemEntity since updateCartItems expects CartItemEntity
                        val cartItemEntities = items.map { item ->
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
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun updateCartItems(cartItemEntities: List<CartItemEntity>) {
        if (cartItemEntities.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvCartItems.visibility = View.GONE
            binding.cardSummary.visibility = View.GONE
            binding.btnCheckout.visibility = View.GONE
            binding.couponLayout.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvCartItems.visibility = View.VISIBLE
            binding.cardSummary.visibility = View.VISIBLE
            binding.btnCheckout.visibility = View.VISIBLE
            binding.couponLayout.visibility = View.VISIBLE
            
            // Convert CartItemEntity to CartItem
            cartItems = cartItemEntities.map { entity ->
                CartItem(
                    id = entity.id,
                    product = entity.product ?: createPlaceholderProduct(entity.productId),
                    quantity = entity.quantity,
                    price = entity.product?.price ?: 0.0,
                    cartId = entity.cartId
                )
            }
            
            cartAdapter.updateItems(cartItems)
            
            // Update summary with fresh calculation
            calculateCartTotal()
            updateCartSummary()
        }
    }
    
    private fun calculateCartTotal() {
        cartTotal = cartItems.sumOf { it.price * it.quantity }
        
        // Reset shipping cost based on cart total
        shippingCost = if (cartTotal >= 1000) 0.0 else 50.0
        
        // Reset discount if coupon was applied
        if (appliedCoupon != null) {
            when (appliedCoupon?.uppercase()) {
                "WELCOME10" -> discount = cartTotal * 0.1
                "TIRUVEAR20" -> discount = cartTotal * 0.2
                "FREESHIP" -> shippingCost = 0.0
                else -> discount = 0.0
            }
        }
    }
    
    private fun updateCartSummary() {
        binding.tvSubtotalValue.text = "₹${String.format("%.2f", cartTotal)}"
        binding.tvShippingValue.text = "₹${String.format("%.2f", shippingCost)}"
        
        // Add discount section if applicable
        if (discount > 0) {
            binding.discountLayout.visibility = View.VISIBLE
            binding.tvDiscountValue.text = "-₹${String.format("%.2f", discount)}"
        } else {
            binding.discountLayout.visibility = View.GONE
        }
        
        // Show free shipping message if applicable
        if (shippingCost == 0) {
            binding.tvShippingLabel.text = "Shipping (Free)"
        } else {
            binding.tvShippingLabel.text = "Shipping"
        }
        
        val finalTotal = cartTotal + shippingCost - discount
        binding.tvTotalValue.text = "₹${String.format("%.2f", finalTotal)}"
        
        // Update checkout button
        binding.btnCheckout.text = "Checkout (₹${String.format("%.2f", finalTotal)})"
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
    
    private fun removeFromCart(item: CartItem) {
        coroutineScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                
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
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateQuantity(item: CartItem, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(item)
            return
        }
        
        coroutineScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                
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
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean, message: String = "Loading...") {
        if (isLoading) {
            binding.loadingText.text = message
            binding.loadingLayout.visibility = View.VISIBLE
        } else {
            binding.loadingLayout.visibility = View.GONE
        }
    }
    
    private fun createPlaceholderProduct(productId: String): Product {
        return Product(
            id = productId,
            name = "Product",
            description = "Product description",
            basePrice = 0.0,
            categoryId = "",
            stockQuantity = 0,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
} 