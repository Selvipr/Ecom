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
        }
        
        setupUI()
        setupRecyclerView()
        loadProducts()
    }
    
    private fun setupUI() {
        binding.tvCategoryTitle.text = categoryName ?: "Products"
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
                val productsResult = withContext(Dispatchers.IO) {
                    categoryId?.let {
                        productRepository.getProductsByCategory(it, page, limit)
                    } ?: productRepository.getAllProducts(page, limit)
                }
                
                if (productsResult?.isSuccess == true) {
                    val products = productsResult.getOrNull() ?: emptyList()
                    updateProducts(products)
                } else {
                    Log.e(TAG, "Error loading products: ${productsResult?.exceptionOrNull()?.message}")
                    showError("Failed to load products")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading products: ${e.message}", e)
                showError("An error occurred while loading products")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateProducts(products: List<Product>) {
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