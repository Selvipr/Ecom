package com.tiruvear.textiles.ui.main.orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.Order
import com.tiruvear.textiles.data.repositories.OrderRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentOrdersBinding
import com.tiruvear.textiles.ui.main.adapters.OrderAdapter
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdersFragment : BaseFragment<FragmentOrdersBinding>() {
    override val TAG: String = "OrdersFragment"
    
    private val orderRepository by lazy { OrderRepositoryImpl() }
    private lateinit var orderAdapter: OrderAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOrdersBinding {
        return FragmentOrdersBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupRecyclerView()
        loadOrders()
    }
    
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            orders = emptyList(),
            onTrackOrder = { order -> trackOrder(order) },
            onViewDetails = { order -> viewOrderDetails(order) }
        )
        
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }
    
    private fun loadOrders() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        coroutineScope.launch {
            try {
                val userId = getCurrentUserId() ?: "guest_user"
                val result = withContext(Dispatchers.IO) {
                    orderRepository.getUserOrders()
                }
                
                if (result.isSuccess) {
                    val ordersList = result.getOrNull() ?: emptyList()
                    updateOrders(ordersList)
                } else {
                    Log.e(TAG, "Error loading orders: ${result.exceptionOrNull()?.message}")
                    showError("Failed to load orders")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading orders: ${e.message}", e)
                showError("An error occurred while loading orders")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun updateOrders(orders: List<Order>) {
        if (orders.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvOrders.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvOrders.visibility = View.VISIBLE
            
            val updatedOrders = ArrayList(orders)
            orderAdapter = OrderAdapter(
                orders = updatedOrders,
                onTrackOrder = { order -> trackOrder(order) },
                onViewDetails = { order -> viewOrderDetails(order) }
            )
            binding.rvOrders.adapter = orderAdapter
        }
    }
    
    private fun trackOrder(order: Order) {
        // In a real app, navigate to order tracking screen
        val bundle = Bundle().apply {
            putString("orderId", order.id)
        }
        
        try {
            findNavController().navigate(R.id.action_navigation_orders_to_orderDetailFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to track order: ${e.message}", e)
            showToast("Order tracking for ${order.id} coming soon!")
        }
    }
    
    private fun viewOrderDetails(order: Order) {
        // In a real app, navigate to order details screen
        val bundle = Bundle().apply {
            putString("orderId", order.id)
        }
        
        try {
            findNavController().navigate(R.id.action_navigation_orders_to_orderDetailFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to order details: ${e.message}", e)
            showToast("Order details for ${order.id} coming soon!")
        }
    }
    
    private fun showError(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvOrders.visibility = View.GONE
    }
} 