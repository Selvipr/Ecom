package com.tiruvear.textiles.ui.main.products

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.data.repositories.CartRepositoryImpl
import com.tiruvear.textiles.data.repositories.ProductRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentProductListBinding
import com.tiruvear.textiles.ui.main.adapters.ProductAdapter
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductListFragment : BaseFragment<FragmentProductListBinding>() {
    override val TAG: String = "ProductListFragment"
    
    private val productRepository by lazy { ProductRepositoryImpl() }
    private val cartRepository by lazy { CartRepositoryImpl() }
    private lateinit var productAdapter: ProductAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    private var categoryId: String? = null
    private var categoryName: String? = null
    private var searchQuery: String? = null
    private var listType: String? = null // "featured", "new", or null (for category or search)
    private var title: String? = null
    private var page = 1
    private val limit = 20
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProductListBinding {
        return FragmentProductListBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        arguments?.let {
            categoryId = it.getString("categoryId")
            categoryName = it.getString("categoryName")
            searchQuery = it.getString("query")
            listType = it.getString("type")
            title = it.getString("title")
        }
        
        setupUI()
        setupRecyclerView()
        loadProducts()
    }
    
    private fun setupUI() {
        // Set the appropriate title based on the arguments
        when {
            title != null -> binding.tvCategoryTitle.text = title
            categoryName != null -> binding.tvCategoryTitle.text = categoryName
            searchQuery != null -> binding.tvCategoryTitle.text = "Search: $searchQuery"
            else -> binding.tvCategoryTitle.text = "Products"
        }
        
        // Add back button functionality
        binding.btnBack.setOnClickListener {
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating back: ${e.message}", e)
            }
        }
    }
    
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = emptyList(),
            onProductClicked = { product -> navigateToProductDetail(product) },
            onAddToCartClicked = { product -> addToCart(product) },
            onBuyNowClicked = { product -> buyNow(product) }
        )
        
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }
    
    private fun loadProducts() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        coroutineScope.launch {
            try {
                val result = when {
                    // If search query is provided, search for products
                    searchQuery != null -> {
                        withContext(Dispatchers.IO) {
                            productRepository.searchProducts(searchQuery!!, page, limit)
                        }
                    }
                    // If category ID is provided, get products for that category
                    categoryId != null -> {
                        withContext(Dispatchers.IO) {
                            productRepository.getProductsByCategory(categoryId!!, page, limit)
                        }
                    }
                    // If listType is "featured", get featured products
                    listType == "featured" -> {
                        withContext(Dispatchers.IO) {
                            productRepository.getFeaturedProducts(limit * 2) // Show more in the all view
                        }
                    }
                    // If listType is "new", get new arrivals
                    listType == "new" -> {
                        withContext(Dispatchers.IO) {
                            productRepository.getNewArrivals(limit * 2) // Show more in the all view
                        }
                    }
                    // Default to get all products
                    else -> {
                        withContext(Dispatchers.IO) {
                            productRepository.getAllProducts(page, limit)
                        }
                    }
                }
                
                binding.progressBar.visibility = View.GONE
                
                if (result.isSuccess) {
                    val products = result.getOrNull() ?: emptyList()
                    updateProductList(products)
                } else {
                    showError("Failed to load products")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "Error loading products: ${e.message}", e)
                showError("Failed to load products")
            }
        }
    }
    
    private fun updateProductList(products: List<Product>) {
        if (products.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE
            productAdapter = ProductAdapter(
                products = products,
                onProductClicked = { product -> navigateToProductDetail(product) },
                onAddToCartClicked = { product -> addToCart(product) },
                onBuyNowClicked = { product -> buyNow(product) }
            )
            binding.rvProducts.adapter = productAdapter
        }
    }
    
    private fun navigateToProductDetail(product: Product) {
        val bundle = Bundle().apply {
            putString("productId", product.id)
        }
        findNavController().navigate(R.id.action_productListFragment_to_productDetailFragment, bundle)
    }
    
    private fun addToCart(product: Product) {
        binding.progressBar.visibility = View.VISIBLE
        
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId() ?: "guest_user"
                val result = withContext(Dispatchers.IO) {
                    cartRepository.addToCart(userId, product.id, 1)
                }
                
                binding.progressBar.visibility = View.GONE
                
                if (result.isSuccess) {
                    showToast("${product.name} added to cart")
                    
                    // Show confirmation dialog with option to view cart
                    showAddToCartConfirmation()
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Failed to add to cart"
                    showToast("Error: $errorMessage")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
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
                    findNavController().navigate(R.id.navigation_cart)
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to cart: ${e.message}", e)
                    showToast("Unable to navigate to cart")
                }
            }
            .setNegativeButton("Continue Shopping") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun buyNow(product: Product) {
        // First add to cart, then navigate to checkout
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId() ?: "guest_user"
                val result = withContext(Dispatchers.IO) {
                    cartRepository.addToCart(userId, product.id, 1)
                }
                
                if (result.isSuccess) {
                    // Navigate to checkout
                    findNavController().navigate(R.id.action_productListFragment_to_cartFragment)
                } else {
                    showToast("Failed to proceed to checkout")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error buying product: ${e.message}", e)
                showToast("Error proceeding to checkout")
            }
        }
    }
    
    private fun showError(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvProducts.visibility = View.GONE
    }
} 