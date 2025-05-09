package com.tiruvear.textiles.ui.main.orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.tiruvear.textiles.databinding.FragmentOrderDetailBinding
import com.tiruvear.textiles.ui.main.adapters.OrderItemAdapter
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OrderDetailFragment : BaseFragment<FragmentOrderDetailBinding>() {
    override val TAG: String = "OrderDetailFragment"
    
    private val orderRepository: OrderRepository by lazy { OrderRepositoryImpl() }
    private lateinit var orderItemAdapter: OrderItemAdapter
    private var currentOrder: Order? = null
    private val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private lateinit var orderId: String
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOrderDetailBinding {
        return FragmentOrderDetailBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        // Get orderId from arguments
        orderId = arguments?.getString("orderId") ?: ""
        if (orderId.isEmpty()) {
            showError("Invalid order ID")
            return
        }
        
        setupRecyclerView()
        setupToolbar()
        setupRefreshFunctionality()
        loadOrderDetails()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRefreshFunctionality() {
        // Add pull-to-refresh functionality if needed
        binding.errorLayout.findViewById<View>(R.id.btn_retry)?.setOnClickListener {
            loadOrderDetails()
        }
    }
    
    private fun setupRecyclerView() {
        orderItemAdapter = OrderItemAdapter(
            items = emptyList(),
            onItemClick = { orderItem ->
                navigateToProductDetail(orderItem.product.id)
            }
        )
        binding.rvOrderItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderItemAdapter
        }
    }
    
    private fun loadOrderDetails() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val result = orderRepository.getOrderById(orderId)
                
                if (result.isSuccess) {
                    val order = result.getOrNull()
                    if (order != null) {
                        currentOrder = order
                        displayOrderDetails(order)
                        updateStatusUI(order.status)
                    } else {
                        showError("Order not found")
                    }
                } else {
                    Log.e(TAG, "Error loading order: ${result.exceptionOrNull()?.message}")
                    showError("Failed to load order details: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading order: ${e.message}", e)
                showError("An error occurred while loading order details: ${e.message}")
                
                // For demo purposes, load mock data if there's an error
                loadMockOrderDetails()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun displayOrderDetails(order: Order) {
        binding.tvOrderId.text = order.id
        binding.tvOrderDate.text = dateFormatter.format(order.orderDate)
        binding.tvOrderTotal.text = "₹${String.format("%.2f", order.totalAmount)}"
        binding.tvPaymentMethod.text = order.paymentMethod
        binding.tvShippingAddress.text = formatShippingAddress(order.shippingAddress)
        
        // Set delivery date if available
        if (order.deliveryDate != null) {
            binding.tvDeliveryDate.text = dateFormatter.format(order.deliveryDate)
            binding.deliveryDateLayout.visibility = View.VISIBLE
        } else {
            binding.deliveryDateLayout.visibility = View.GONE
        }
        
        // Set tracking number if available
        if (!order.trackingNumber.isNullOrEmpty()) {
            binding.tvTrackingNumber.text = order.trackingNumber
            binding.trackingNumberLayout.visibility = View.VISIBLE
        } else {
            binding.trackingNumberLayout.visibility = View.GONE
        }
        
        // Update order items
        orderItemAdapter = OrderItemAdapter(
            items = order.items,
            onItemClick = { orderItem ->
                navigateToProductDetail(orderItem.product.id)
            }
        )
        binding.rvOrderItems.adapter = orderItemAdapter
        
        // Set order status with appropriate color
        binding.tvOrderStatus.text = getFormattedStatus(order.status)
        binding.tvOrderStatus.setTextColor(getStatusColor(order.status))
        
        // Setup action buttons based on order status
        setupActionButtons(order)
    }
    
    private fun setupActionButtons(order: Order) {
        // Show/hide cancel button based on order status
        if (order.status == OrderStatus.PENDING || order.status == OrderStatus.CONFIRMED) {
            binding.btnCancelOrder.visibility = View.VISIBLE
            binding.btnCancelOrder.setOnClickListener {
                showCancelOrderConfirmation(order.id)
            }
        } else {
            binding.btnCancelOrder.visibility = View.GONE
        }
        
        // Show track button only for shipped orders
        if (order.status == OrderStatus.SHIPPED && !order.trackingNumber.isNullOrEmpty()) {
            binding.btnTrackShipment.visibility = View.VISIBLE
            binding.btnTrackShipment.setOnClickListener {
                trackShipment(order.trackingNumber)
            }
        } else {
            binding.btnTrackShipment.visibility = View.GONE
        }
        
        // Set up reorder button for all completed orders
        if (order.status == OrderStatus.DELIVERED || order.status == OrderStatus.CANCELLED) {
            binding.btnReorder.visibility = View.VISIBLE
            binding.btnReorder.setOnClickListener {
                reorderItems(order.items)
            }
        } else {
            binding.btnReorder.visibility = View.GONE
        }
    }
    
    private fun updateStatusUI(status: OrderStatus) {
        binding.progressDelivery.progress = when (status) {
            OrderStatus.PENDING -> 10
            OrderStatus.CONFIRMED -> 25
            OrderStatus.PROCESSING -> 50
            OrderStatus.SHIPPED -> 75
            OrderStatus.DELIVERED -> 100
            OrderStatus.CANCELLED, OrderStatus.RETURNED -> 0
        }
        
        // Highlight the current status in the progress indicators
        binding.tvStatusConfirmed.isActivated = status.ordinal >= OrderStatus.CONFIRMED.ordinal
        binding.tvStatusProcessing.isActivated = status.ordinal >= OrderStatus.PROCESSING.ordinal
        binding.tvStatusShipped.isActivated = status.ordinal >= OrderStatus.SHIPPED.ordinal
        binding.tvStatusDelivered.isActivated = status.ordinal >= OrderStatus.DELIVERED.ordinal
        
        // Special case for cancelled orders
        if (status == OrderStatus.CANCELLED) {
            binding.tvStatusDelivered.text = "Cancelled"
            binding.progressDelivery.progress = 0
            binding.progressDelivery.progressTintList = resources.getColorStateList(R.color.error, null)
        } else if (status == OrderStatus.RETURNED) {
            binding.tvStatusDelivered.text = "Returned"
            binding.progressDelivery.progress = 100
            binding.progressDelivery.progressTintList = resources.getColorStateList(R.color.warning, null)
        } else {
            // Reset text for normal order flow
            binding.tvStatusDelivered.text = "Delivered"
            binding.progressDelivery.progressTintList = resources.getColorStateList(R.color.primary, null)
        }
    }
    
    private fun showCancelOrderConfirmation(orderId: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Yes") { _, _ ->
                cancelOrder(orderId)
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
                    val updatedOrder = result.getOrNull()
                    if (updatedOrder != null) {
                        currentOrder = updatedOrder
                        displayOrderDetails(updatedOrder)
                        updateStatusUI(updatedOrder.status)
                        showToast("Order cancelled successfully")
                    } else {
                        showToast("Failed to cancel order")
                    }
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
    
    private fun trackShipment(trackingNumber: String) {
        // In a real app, this would open a shipping tracking page or app
        showTrackingDialog(trackingNumber)
    }
    
    private fun showTrackingDialog(trackingNumber: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Track Shipment")
            .setMessage("Tracking number: $trackingNumber\n\nYour order is on the way and will be delivered soon.")
            .setPositiveButton("OK", null)
        
        builder.create().show()
    }
    
    private fun reorderItems(items: List<OrderItem>) {
        try {
            showToast("Adding items to cart...")
            
            // Here we would implement the actual cart add functionality
            // For now, just simulate adding to cart with a delay
            lifecycleScope.launch {
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                // Navigate to cart fragment
                findNavController().navigate(R.id.action_orderDetailFragment_to_navigation_cart)
                showToast("Items added to cart successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reordering items: ${e.message}", e)
            showToast("Failed to add items to cart")
        }
    }
    
    private fun formatShippingAddress(address: String): String {
        // Simple formatting for demo purposes
        return address.replace(",", ",\n")
    }
    
    private fun getFormattedStatus(status: OrderStatus): String {
        return when (status) {
            OrderStatus.PENDING -> "Pending"
            OrderStatus.CONFIRMED -> "Confirmed"
            OrderStatus.PROCESSING -> "Processing"
            OrderStatus.SHIPPED -> "Shipped"
            OrderStatus.DELIVERED -> "Delivered"
            OrderStatus.CANCELLED -> "Cancelled"
            OrderStatus.RETURNED -> "Returned"
        }
    }
    
    private fun getStatusColor(status: OrderStatus): Int {
        val context = requireContext()
        val resources = context.resources
        val packageName = context.packageName
        
        return when (status) {
            OrderStatus.PENDING -> resources.getIdentifier("warning", "color", packageName)
            OrderStatus.CONFIRMED, OrderStatus.PROCESSING -> resources.getIdentifier("info", "color", packageName)
            OrderStatus.SHIPPED -> resources.getIdentifier("primary", "color", packageName)
            OrderStatus.DELIVERED -> resources.getIdentifier("success", "color", packageName)
            OrderStatus.CANCELLED, OrderStatus.RETURNED -> resources.getIdentifier("error", "color", packageName)
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.scrollView.visibility = View.GONE
            binding.errorLayout.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
        }
    }
    
    private fun showError(message: String) {
        binding.errorLayout.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
        binding.scrollView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }
    
    private fun navigateToProductDetail(productId: String) {
        try {
            val bundle = Bundle().apply {
                putString("productId", productId)
            }
            findNavController().navigate(R.id.action_orderDetailFragment_to_productDetailFragment, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to product detail: ${e.message}", e)
            showToast("Could not open product details")
        }
    }
    
    // This method is just for the demo to ensure something is displayed
    private fun loadMockOrderDetails() {
        val calendar = Calendar.getInstance()
        val orderDate = calendar.time
        
        // Set a delivery date 3 days in the future
        calendar.add(Calendar.DAY_OF_MONTH, 3)
        val deliveryDate = calendar.time
        
        val mockOrder = Order(
            id = orderId.ifEmpty { "ORD-12345678" },
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
                        createdAt = orderDate,
                        updatedAt = orderDate
                    ),
                    quantity = 1,
                    price = 1299.0
                ),
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
                        createdAt = orderDate,
                        updatedAt = orderDate
                    ),
                    quantity = 2,
                    price = 899.0
                )
            ),
            totalAmount = 3097.0,
            shippingAddress = "123 Main St, Coimbatore, TN 641001",
            status = OrderStatus.SHIPPED,
            paymentMethod = "Cash on Delivery",
            orderDate = orderDate,
            deliveryDate = deliveryDate,
            trackingNumber = "TN123456789"
        )
        
        currentOrder = mockOrder
        displayOrderDetails(mockOrder)
        updateStatusUI(mockOrder.status)
    }
} 