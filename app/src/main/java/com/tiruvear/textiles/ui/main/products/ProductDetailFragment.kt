package com.tiruvear.textiles.ui.main.products

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.data.repositories.CartRepositoryImpl
import com.tiruvear.textiles.data.repositories.ProductRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentProductDetailBinding
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment : BaseFragment<FragmentProductDetailBinding>() {
    override val TAG: String = "ProductDetailFragment"
    
    private val productRepository by lazy { ProductRepositoryImpl() }
    private val cartRepository by lazy { CartRepositoryImpl() }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    private var productId: String? = null
    private var product: Product? = null
    private var quantity: Int = 1
    
    init {
        currencyFormat.maximumFractionDigits = 0
    }
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProductDetailBinding {
        return FragmentProductDetailBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        arguments?.let {
            productId = it.getString("productId")
        }
        
        setupClickListeners()
        setupQuantityControls()
        loadProductDetails()
    }
    
    private fun setupClickListeners() {
        binding.btnAddToCart.setOnClickListener {
            binding.btnAddToCart.isEnabled = false
            addToCart()
        }
        
        binding.btnBuyNow.setOnClickListener {
            binding.btnBuyNow.isEnabled = false
            buyNow()
        }
        
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupQuantityControls() {
        binding.tvQuantity.text = quantity.toString()
        
        binding.btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }
        
        binding.btnIncrease.setOnClickListener {
            val maxStock = product?.stockQuantity ?: 10
            if (quantity < maxStock) {
                quantity++
                binding.tvQuantity.text = quantity.toString()
            } else {
                showToast("Maximum available stock: $maxStock")
            }
        }
    }
    
    private fun loadProductDetails() {
        binding.apply {
            // Reset visibility while loading
            progressBar.visibility = View.VISIBLE
            ivProduct.visibility = View.GONE
            tvProductName.visibility = View.GONE
            tvProductPrice.visibility = View.GONE
            tvProductSalePrice.visibility = View.GONE
            tvProductDescriptionLabel.visibility = View.GONE
            tvProductDescription.visibility = View.GONE
            llButtons.visibility = View.GONE
            quantityLayout.visibility = View.GONE
        }
        
        val productId = this.productId ?: return
        
        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    productRepository.getProductById(productId)
                }
                
                binding.progressBar.visibility = View.GONE
                
                if (result.isSuccess) {
                    product = result.getOrNull()
                    displayProductDetails()
                } else {
                    showToast("Failed to load product details")
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "Error loading product details: ${e.message}", e)
                showToast("Error loading product details")
                findNavController().popBackStack()
            }
        }
    }
    
    private fun displayProductDetails() {
        val product = this.product ?: return
        
        binding.apply {
            // Set product name and description
            tvProductName.text = product.name
            tvProductName.visibility = View.VISIBLE
            
            tvProductDescription.text = product.description
            tvProductDescription.visibility = View.VISIBLE
            tvProductDescriptionLabel.visibility = View.VISIBLE
            
            // Format and display prices
            tvProductPrice.text = currencyFormat.format(product.basePrice)
            tvProductPrice.visibility = View.VISIBLE
            
            if (product.salePrice != null && product.salePrice < product.basePrice) {
                tvProductSalePrice.text = currencyFormat.format(product.basePrice)
                tvProductSalePrice.paintFlags = tvProductSalePrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvProductSalePrice.visibility = View.VISIBLE
                tvProductPrice.text = currencyFormat.format(product.salePrice)
            } else {
                tvProductSalePrice.visibility = View.GONE
            }
            
            // Show stock information
            tvStock.text = "In Stock: ${product.stockQuantity}"
            tvStock.visibility = View.VISIBLE
            
            // Load product image
            Glide.with(requireContext())
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(ivProduct)
            ivProduct.visibility = View.VISIBLE
            
            // Show action buttons and quantity controls
            llButtons.visibility = View.VISIBLE
            quantityLayout.visibility = View.VISIBLE
        }
    }
    
    private fun addToCart() {
        val product = this.product ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId() ?: "guest_user"
                val result = withContext(Dispatchers.IO) {
                    cartRepository.addToCart(userId, product.id, quantity)
                }
                
                binding.progressBar.visibility = View.GONE
                binding.btnAddToCart.isEnabled = true
                
                if (result.isSuccess) {
                    showToast("${product.name} added to cart")
                    showAddToCartConfirmation()
                } else {
                    showToast("Failed to add to cart")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnAddToCart.isEnabled = true
                Log.e(TAG, "Error adding to cart: ${e.message}", e)
                showToast("Error adding to cart")
            }
        }
    }
    
    private fun showAddToCartConfirmation() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Item Added to Cart")
            .setMessage("Item has been added to your cart. What would you like to do?")
            .setPositiveButton("View Cart") { _, _ ->
                try {
                    findNavController().navigate(R.id.action_productDetailFragment_to_cartFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to cart: ${e.message}", e)
                    findNavController().navigate(R.id.navigation_cart)
                }
            }
            .setNegativeButton("Continue Shopping") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun buyNow() {
        val product = this.product ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId() ?: "guest_user"
                val result = withContext(Dispatchers.IO) {
                    cartRepository.addToCart(userId, product.id, quantity)
                }
                
                binding.progressBar.visibility = View.GONE
                binding.btnBuyNow.isEnabled = true
                
                if (result.isSuccess) {
                    // Navigate to checkout
                    try {
                        findNavController().navigate(R.id.action_productDetailFragment_to_cartFragment)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error navigating to cart: ${e.message}", e)
                        findNavController().navigate(R.id.navigation_cart)
                    }
                } else {
                    showToast("Failed to proceed to checkout")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnBuyNow.isEnabled = true
                Log.e(TAG, "Error buying product: ${e.message}", e)
                showToast("Error proceeding to checkout")
            }
        }
    }
} 