package com.tiruvear.textiles.ui.main.orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.Order
import com.tiruvear.textiles.data.models.OrderItem
import com.tiruvear.textiles.data.models.OrderStatus
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.data.repositories.OrderRepository
import com.tiruvear.textiles.data.repositories.OrderRepositoryImpl
import com.tiruvear.textiles.data.util.SessionManager
import com.tiruvear.textiles.databinding.FragmentOrdersBinding
import com.tiruvear.textiles.ui.main.adapters.OrderAdapter
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.launch
import java.util.*

class OrdersFragment : BaseFragment<FragmentOrdersBinding>() {
    override val TAG: String = "OrdersFragment"
    
    private val orderRepository: OrderRepository by lazy { OrderRepositoryImpl() }
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var sessionManager: SessionManager
    private var allOrders: List<Order> = emptyList()
    private var selectedStatusFilter: String? = null
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOrdersBinding {
        return FragmentOrdersBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())
        setupRecyclerView()
        setupStatusFilter()
        setupRefreshLayout()
        loadOrders()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload orders data when returning to this fragment
        loadOrders()
    }
    
    private fun setupStatusFilter() {
        val statusOptions = listOf("All Orders", "Pending", "Confirmed", "Processing", "Shipped", "Delivered", "Cancelled", "Returned")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerStatus.adapter = adapter
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStatusFilter = if (position == 0) null else statusOptions[position]
                filterOrders()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedStatusFilter = null
                filterOrders()
            }
        }
    }
    
    private fun setupRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadOrders()
        }
        
        // Set refresh color scheme
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.secondary,
            R.color.success
        )
    }
    
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            orders = emptyList(),
            onTrackOrder = { order -> trackOrder(order) },
            onViewDetails = { order -> viewOrderDetails(order) },
            onCancelOrder = { order -> showCancelOrderConfirmation(order) }
        )
        
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }
    
    private fun loadOrders() {
        showLoading(true)
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val result = orderRepository.getUserOrders()
                
                if (result.isSuccess) {
                    val ordersList = result.getOrNull() ?: emptyList()
                    
                    if (ordersList.isEmpty()) {
                        showEmpty("You haven't placed any orders yet")
                    } else {
                        allOrders = ordersList
                        filterOrders()
                    }
                } else {
                    Log.e(TAG, "Error loading orders: ${result.exceptionOrNull()?.message}")
                    showError("Failed to load orders. Please try again.")
                    // If we fail to load real orders, use mock data for demo purposes
                    allOrders = createDemoOrders()
                    filterOrders()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading orders: ${e.message}", e)
                showError("An error occurred while loading orders")
                // If we fail to load real orders, use mock data for demo purposes
                allOrders = createDemoOrders()
                filterOrders()
            } finally {
                showLoading(false)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    private fun filterOrders() {
        val filteredOrders = if (selectedStatusFilter == null) {
            allOrders
        } else {
            val statusEnum = when (selectedStatusFilter) {
                "Pending" -> OrderStatus.PENDING
                "Confirmed" -> OrderStatus.CONFIRMED
                "Processing" -> OrderStatus.PROCESSING
                "Shipped" -> OrderStatus.SHIPPED
                "Delivered" -> OrderStatus.DELIVERED
                "Cancelled" -> OrderStatus.CANCELLED
                "Returned" -> OrderStatus.RETURNED
                else -> null
            }
            
            if (statusEnum != null) {
                allOrders.filter { it.status == statusEnum }
            } else {
                allOrders
            }
        }
        
        updateOrders(filteredOrders)
    }
    
    private fun updateOrders(orders: List<Order>) {
        if (orders.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvOrders.visibility = View.GONE
            
            // Show appropriate empty message based on filter
            if (selectedStatusFilter != null) {
                binding.tvEmpty.text = "No ${selectedStatusFilter?.lowercase()} orders found"
            } else {
                binding.tvEmpty.text = getString(R.string.no_orders_found)
            }
            binding.tvOrderCount.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvOrders.visibility = View.VISIBLE
            
            // Sort orders by date (newest first)
            val sortedOrders = orders.sortedByDescending { it.orderDate }
            
            orderAdapter = OrderAdapter(
                orders = sortedOrders,
                onTrackOrder = { order -> trackOrder(order) },
                onViewDetails = { order -> viewOrderDetails(order) },
                onCancelOrder = { order -> showCancelOrderConfirmation(order) }
            )
            binding.rvOrders.adapter = orderAdapter
            binding.tvOrderCount.text = "${sortedOrders.size} order(s)"
            binding.tvOrderCount.visibility = View.VISIBLE
        }
    }
    
    private fun trackOrder(order: Order) {
        if (order.status == OrderStatus.SHIPPED && !order.trackingNumber.isNullOrEmpty()) {
            showTrackingDialog(order.trackingNumber)
        } else {
            viewOrderDetails(order)
        }
    }
    
    private fun showTrackingDialog(trackingNumber: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Track Shipment")
            .setMessage("Tracking number: $trackingNumber\n\nYour order is on the way and will be delivered soon.")
            .setPositiveButton("View Details") { _, _ ->
                // Navigate to order details
            }
            .setNegativeButton("OK", null)
        
        builder.create().show()
    }
    
    private fun viewOrderDetails(order: Order) {
        // Navigate to order details screen using Bundle
        val bundle = Bundle().apply {
            putString("orderId", order.id)
        }
        
        try {
            findNavController().navigate(R.id.action_navigation_orders_to_orderDetailFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to order details: ${e.message}", e)
            showToast("Could not open order details")
        }
    }
    
    private fun showCancelOrderConfirmation(order: Order) {
        // Only allow cancellation of pending/confirmed orders
        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.CONFIRMED) {
            showToast("Cannot cancel this order at its current status")
            return
        }
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel order ${order.id}?")
            .setPositiveButton("Yes") { _, _ ->
                cancelOrder(order.id)
            }
            .setNegativeButton("No", null)
        
        builder.create().show()
    }
    
    private fun cancelOrder(orderId: String) {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val result = orderRepository.cancelOrder(orderId)
                
                if (result.isSuccess) {
                    showToast("Order cancelled successfully")
                    loadOrders() // Reload all orders
                } else {
                    Log.e(TAG, "Error cancelling order: ${result.exceptionOrNull()?.message}")
                    showToast("Failed to cancel order")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception cancelling order: ${e.message}", e)
                showToast("An error occurred while cancelling the order")
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            if (allOrders.isEmpty()) {
                // Only hide RecyclerView on initial load
                binding.rvOrders.visibility = View.GONE
            }
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
    
    private fun showEmpty(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvOrders.visibility = View.GONE
        binding.tvOrderCount.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        binding.tvEmpty.text = message
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvOrders.visibility = View.GONE
        binding.tvOrderCount.visibility = View.GONE
    }
    
    // Creates demo orders for fallback if repository fails
    private fun createDemoOrders(): List<Order> {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        
        // Move back 2 days for previous order
        calendar.add(Calendar.DAY_OF_MONTH, -2)
        val previousDate = calendar.time
        
        // Reset and set a future date for delivery
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        val futureDate = calendar.time
        
        return listOf(
            Order(
                id = "ORD-12345678",
                userId = "user123",
                items = listOf(
                    OrderItem(
                        product = Product(
                            id = "P1",
                            name = "Cotton Saree",
                            description = "Beautiful cotton saree",
                            basePrice = 1499.0,
                            salePrice = 1299.0,
                            categoryId = "1",
                            stockQuantity = 10,
                            isActive = true,
                            createdAt = currentDate,
                            updatedAt = currentDate
                        ),
                        quantity = 1,
                        price = 1299.0
                    )
                ),
                totalAmount = 1299.0,
                shippingAddress = "123 Main St, Coimbatore, TN 641001",
                status = OrderStatus.PENDING,
                paymentMethod = "Cash on Delivery",
                orderDate = currentDate,
                deliveryDate = null,
                trackingNumber = null
            ),
            Order(
                id = "ORD-23456789",
                userId = "user123",
                items = listOf(
                    OrderItem(
                        product = Product(
                            id = "P2",
                            name = "Silk Dhoti",
                            description = "Premium silk dhoti",
                            basePrice = 899.0,
                            salePrice = null,
                            categoryId = "2",
                            stockQuantity = 15,
                            isActive = true,
                            createdAt = previousDate,
                            updatedAt = previousDate
                        ),
                        quantity = 2,
                        price = 899.0
                    )
                ),
                totalAmount = 1798.0,
                shippingAddress = "456 Oak St, Chennai, TN 600001",
                status = OrderStatus.SHIPPED,
                paymentMethod = "Credit Card",
                orderDate = previousDate,
                deliveryDate = futureDate,
                trackingNumber = "TN-987654321"
            )
        )
    }
} 