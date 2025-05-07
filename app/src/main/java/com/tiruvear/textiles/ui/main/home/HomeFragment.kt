package com.tiruvear.textiles.ui.main.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.Banner
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.data.models.ProductCategory
import com.tiruvear.textiles.data.repositories.ProductRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentHomeBinding
import com.tiruvear.textiles.ui.main.adapters.BannerAdapter
import com.tiruvear.textiles.ui.main.adapters.CategoryAdapter
import com.tiruvear.textiles.ui.main.adapters.ProductAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val productRepository by lazy { ProductRepositoryImpl() }
    
    private lateinit var featuredProductsAdapter: ProductAdapter
    private lateinit var newArrivalsAdapter: ProductAdapter
    private lateinit var categoriesAdapter: CategoryAdapter
    private lateinit var bannerAdapter: BannerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            setupBannerSlider()
            setupRecyclerViews()
            setupSearchBar()
            loadData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(requireContext(), "An error occurred while setting up the home screen", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupBannerSlider() {
        // Sample banners - in a real app, these would come from the API
        val banners = listOf(
            Banner(1, "Summer Sale", "Up to 50% off", R.drawable.logo),
            Banner(2, "New Collection", "Check out our latest styles", R.drawable.logo),
            Banner(3, "Free Shipping", "On orders over ₹1000", R.drawable.logo)
        )
        
        bannerAdapter = BannerAdapter(banners)
        binding.viewPagerBanners.adapter = bannerAdapter
        
        // Auto-scroll the banner
        binding.viewPagerBanners.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Auto-scroll logic could be added here
            }
        })
        
        // Set up the indicator
        TabLayoutMediator(binding.bannerIndicator, binding.viewPagerBanners) { _, _ -> 
            // No text for the tabs
        }.attach()
    }
    
    private fun setupSearchBar() {
        binding.etSearch.setOnClickListener {
            // Navigate to search screen or show search dialog
            Toast.makeText(requireContext(), "Search feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupRecyclerViews() {
        // Setup Featured Products RecyclerView
        featuredProductsAdapter = ProductAdapter(
            products = emptyList(),
            onProductClicked = { product -> navigateToProductDetail(product) },
            onAddToCartClicked = { product -> addProductToCart(product) },
            onBuyNowClicked = { product -> buyProductNow(product) }
        )
        binding.rvFeaturedProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredProductsAdapter
        }
        
        // Setup New Arrivals RecyclerView
        newArrivalsAdapter = ProductAdapter(
            products = emptyList(),
            onProductClicked = { product -> navigateToProductDetail(product) },
            onAddToCartClicked = { product -> addProductToCart(product) },
            onBuyNowClicked = { product -> buyProductNow(product) }
        )
        binding.rvNewArrivals.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = newArrivalsAdapter
        }
        
        // Setup Categories RecyclerView
        categoriesAdapter = CategoryAdapter(
            categories = emptyList(),
            onCategoryClicked = { category -> navigateToCategory(category) }
        )
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoriesAdapter
        }
        
        // Setup click listeners for "View All" buttons
        binding.tvFeaturedViewAll.setOnClickListener {
            Toast.makeText(requireContext(), "View all featured products", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to all featured products
        }
        
        binding.tvNewArrivalsViewAll.setOnClickListener {
            Toast.makeText(requireContext(), "View all new arrivals", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to all new arrivals
        }
        
        binding.tvCategoriesViewAll.setOnClickListener {
            Toast.makeText(requireContext(), "View all categories", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to all categories
        }
    }
    
    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Load featured products
                val featuredProductsResult = productRepository.getFeaturedProducts(6)
                if (featuredProductsResult.isSuccess) {
                    val products = featuredProductsResult.getOrNull() ?: emptyList()
                    updateFeaturedProducts(products)
                } else {
                    Log.e(TAG, "Error loading featured products: ${featuredProductsResult.exceptionOrNull()?.message}")
                }
                
                // Load new arrivals
                val newArrivalsResult = productRepository.getNewArrivals(6)
                if (newArrivalsResult.isSuccess) {
                    val products = newArrivalsResult.getOrNull() ?: emptyList()
                    updateNewArrivals(products)
                } else {
                    Log.e(TAG, "Error loading new arrivals: ${newArrivalsResult.exceptionOrNull()?.message}")
                }
                
                // Load categories
                val categoriesResult = productRepository.getAllCategories()
                if (categoriesResult.isSuccess) {
                    val categories = categoriesResult.getOrNull() ?: emptyList()
                    updateCategories(categories)
                } else {
                    Log.e(TAG, "Error loading categories: ${categoriesResult.exceptionOrNull()?.message}")
                }
                
                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "Error loading data: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to load data. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateFeaturedProducts(products: List<Product>) {
        try {
            featuredProductsAdapter = ProductAdapter(
                products = products,
                onProductClicked = { product -> navigateToProductDetail(product) },
                onAddToCartClicked = { product -> addProductToCart(product) },
                onBuyNowClicked = { product -> buyProductNow(product) }
            )
            binding.rvFeaturedProducts.adapter = featuredProductsAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Error updating featured products: ${e.message}", e)
        }
    }
    
    private fun updateNewArrivals(products: List<Product>) {
        try {
            newArrivalsAdapter = ProductAdapter(
                products = products,
                onProductClicked = { product -> navigateToProductDetail(product) },
                onAddToCartClicked = { product -> addProductToCart(product) },
                onBuyNowClicked = { product -> buyProductNow(product) }
            )
            binding.rvNewArrivals.adapter = newArrivalsAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Error updating new arrivals: ${e.message}", e)
        }
    }
    
    private fun updateCategories(categories: List<ProductCategory>) {
        try {
            categoriesAdapter = CategoryAdapter(
                categories = categories,
                onCategoryClicked = { category -> navigateToCategory(category) }
            )
            binding.rvCategories.adapter = categoriesAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Error updating categories: ${e.message}", e)
        }
    }
    
    private fun navigateToProductDetail(product: Product) {
        try {
            // Navigate to product detail screen
            val bundle = Bundle().apply {
                putString("productId", product.id)
            }
            findNavController().navigate(R.id.action_navigation_home_to_productDetailFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to product detail: ${e.message}", e)
            Toast.makeText(requireContext(), "Unable to open product details", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addProductToCart(product: Product) {
        try {
            // TODO: Add product to cart
            Toast.makeText(requireContext(), "Added to cart: ${product.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to cart: ${e.message}", e)
            Toast.makeText(requireContext(), "Unable to add to cart", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun buyProductNow(product: Product) {
        try {
            // First add to cart, then navigate to cart
            Toast.makeText(requireContext(), "Buying now: ${product.name}", Toast.LENGTH_SHORT).show()
            
            // Navigate to cart/checkout
            findNavController().navigate(R.id.action_navigation_home_to_navigation_cart)
        } catch (e: Exception) {
            Log.e(TAG, "Error buying product: ${e.message}", e)
            Toast.makeText(requireContext(), "Unable to proceed to checkout", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToCategory(category: ProductCategory) {
        try {
            // Navigate to category products screen
            val bundle = Bundle().apply {
                putString("categoryId", category.id)
                putString("categoryName", category.name)
            }
            findNavController().navigate(R.id.action_navigation_home_to_productListFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to category: ${e.message}", e)
            Toast.makeText(requireContext(), "Unable to open category", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 