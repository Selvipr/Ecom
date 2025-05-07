package com.tiruvear.textiles.ui.main.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.ProductCategory
import com.tiruvear.textiles.data.repositories.ProductRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentCategoriesBinding
import com.tiruvear.textiles.ui.main.adapters.CategoryAdapter
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesFragment : BaseFragment<FragmentCategoriesBinding>() {
    override val TAG: String = "CategoriesFragment"
    
    private val productRepository by lazy { ProductRepositoryImpl() }
    private lateinit var categoryAdapter: CategoryAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCategoriesBinding {
        return FragmentCategoriesBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupRecyclerView()
        loadCategories()
    }
    
    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories = emptyList(),
            onCategoryClicked = { category -> navigateToCategory(category) }
        )
        
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
        }
    }
    
    private fun loadCategories() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        coroutineScope.launch {
            try {
                val categoriesResult = withContext(Dispatchers.IO) {
                    productRepository.getAllCategories()
                }
                
                if (categoriesResult.isSuccess) {
                    val categories = categoriesResult.getOrNull() ?: emptyList()
                    updateCategories(categories)
                } else {
                    Log.e(TAG, "Error loading categories: ${categoriesResult.exceptionOrNull()?.message}")
                    showError("Failed to load categories")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading categories: ${e.message}", e)
                showError("An error occurred while loading categories")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateCategories(categories: List<ProductCategory>) {
        if (categories.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvCategories.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvCategories.visibility = View.VISIBLE
            categoryAdapter = CategoryAdapter(
                categories = categories,
                onCategoryClicked = { category -> navigateToCategory(category) }
            )
            binding.rvCategories.adapter = categoryAdapter
        }
    }
    
    private fun navigateToCategory(category: ProductCategory) {
        // Navigate to product list fragment with category ID
        val bundle = Bundle().apply {
            putString("categoryId", category.id)
            putString("categoryName", category.name)
        }
        findNavController().navigate(R.id.action_categoriesFragment_to_productListFragment, bundle)
    }
    
    private fun showError(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvCategories.visibility = View.GONE
    }
} 