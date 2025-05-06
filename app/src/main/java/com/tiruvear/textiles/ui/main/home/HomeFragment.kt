package com.tiruvear.textiles.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tiruvear.textiles.data.repositories.ProductRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentHomeBinding
import com.tiruvear.textiles.ui.main.adapters.CategoryAdapter
import com.tiruvear.textiles.ui.main.adapters.ProductAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val productRepository by lazy { ProductRepositoryImpl() }
    
    private lateinit var featuredProductsAdapter: ProductAdapter
    private lateinit var newArrivalsAdapter: ProductAdapter
    private lateinit var categoriesAdapter: CategoryAdapter

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
        
        setupRecyclerViews()
        loadData()
    }
    
    private fun setupRecyclerViews() {
        // Setup Featured Products RecyclerView
        featuredProductsAdapter = ProductAdapter()
        binding.rvFeaturedProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = featuredProductsAdapter
        }
        
        // Setup New Arrivals RecyclerView
        newArrivalsAdapter = ProductAdapter()
        binding.rvNewArrivals.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = newArrivalsAdapter
        }
        
        // Setup Categories RecyclerView
        categoriesAdapter = CategoryAdapter()
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoriesAdapter
        }
    }
    
    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Load featured products
                val featuredProductsResult = productRepository.getFeaturedProducts(6)
                if (featuredProductsResult.isSuccess) {
                    featuredProductsAdapter.submitList(featuredProductsResult.getOrNull())
                }
                
                // Load new arrivals
                val newArrivalsResult = productRepository.getNewArrivals(6)
                if (newArrivalsResult.isSuccess) {
                    newArrivalsAdapter.submitList(newArrivalsResult.getOrNull())
                }
                
                // Load categories
                val categoriesResult = productRepository.getAllCategories()
                if (categoriesResult.isSuccess) {
                    categoriesAdapter.submitList(categoriesResult.getOrNull())
                }
                
                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 