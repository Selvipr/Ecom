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
        loadProductDetails()
    }
    
    private fun setupClickListeners() {
        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }
        
        binding.btnBuyNow.setOnClickListener {
            buyNow()
        }
    }
    
    private fun loadProductDetails() {
        binding.apply {
            // Reset visibility while loading
            ivProduct.visibility = View.GONE
            tvProductName.visibility = View.GONE
            tvProductPrice.visibility = View.GONE
            tvProductSalePrice.visibility = View.GONE
            tvProductDescriptionLabel.visibility = View.GONE
            tvProductDescription.visibility = View.GONE
            llButtons.visibility = View.GONE
        }
        
        val productId = this.productId ?: return
        
        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    productRepository.getProductById(productId)
                }
                
                if (result.isSuccess) {
                    product = result.getOrNull()
                    displayProductDetails()
                } else {
                    showToast("Failed to load product details")
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
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
            
            // Load product image
            Glide.with(requireContext())
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(ivProduct)
            ivProduct.visibility = View.VISIBLE
            
            // Show action buttons
            llButtons.visibility = View.VISIBLE
        }
    }
    
    private fun addToCart() {
        val product = this.product ?: return
        
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId() ?: "guest_user"
                val result = withContext(Dispatchers.IO) {
                    cartRepository.addToCart(userId, product.id, 1)
                }
                
                if (result.isSuccess) {
                    showToast("${product.name} added to cart")
                } else {
                    showToast("Failed to add to cart")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding to cart: ${e.message}", e)
                showToast("Error adding to cart")
            }
        }
    }
    
    private fun buyNow() {
        val product = this.product ?: return
        
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId() ?: "guest_user"
                val result = withContext(Dispatchers.IO) {
                    cartRepository.addToCart(userId, product.id, 1)
                }
                
                if (result.isSuccess) {
                    // Navigate to checkout
                    findNavController().navigate(R.id.action_productDetailFragment_to_cartFragment)
                } else {
                    showToast("Failed to proceed to checkout")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error buying product: ${e.message}", e)
                showToast("Error proceeding to checkout")
            }
        }
    }
} 