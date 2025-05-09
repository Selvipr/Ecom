package com.tiruvear.textiles.ui.main.orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.repositories.OrderRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentOrderConfirmationBinding
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class OrderConfirmationFragment : BaseFragment<FragmentOrderConfirmationBinding>() {
    override val TAG: String = "OrderConfirmationFragment"
    
    private val orderRepository by lazy { OrderRepositoryImpl() }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var orderId: String? = null
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentOrderConfirmationBinding {
        return FragmentOrderConfirmationBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        orderId = arguments?.getString("orderId")
        
        setupListeners()
        loadOrderDetails()
    }
    
    private fun setupListeners() {
        // Continue shopping button
        binding.btnContinueShopping.setOnClickListener {
            // Navigate to home
            findNavController().navigate(R.id.action_orderConfirmationFragment_to_navigation_home)
        }
        
        // View order details button
        binding.btnViewOrder.setOnClickListener {
            // Navigate to order details
            if (orderId != null) {
                val bundle = Bundle().apply {
                    putString("orderId", orderId)
                }
                findNavController().navigate(R.id.action_orderConfirmationFragment_to_orderDetailFragment, bundle)
            } else {
                showToast("Order details not available")
            }
        }
    }
    
    private fun loadOrderDetails() {
        binding.progressBar.visibility = View.VISIBLE
        
        if (orderId == null) {
            // Show generic confirmation if no order ID
            showGenericConfirmation()
            binding.progressBar.visibility = View.GONE
            return
        }
        
        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    orderRepository.getOrderById(orderId!!)
                }
                
                if (result.isSuccess) {
                    val order = result.getOrNull()
                    
                    if (order != null) {
                        // Format date
                        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        val orderDate = dateFormat.format(order.orderDate)
                        
                        // Update UI with order details
                        binding.tvOrderId.text = "Order #${order.id}"
                        binding.tvOrderDate.text = "Placed on $orderDate"
                        binding.tvTotalAmount.text = "₹${String.format("%.2f", order.totalAmount)}"
                        binding.tvItemCount.text = "${order.items.size} item(s)"
                        binding.tvPaymentMethod.text = order.paymentMethod
                        binding.tvShippingAddress.text = order.shippingAddress
                        
                        // Show order-specific section
                        binding.cardOrderInfo.visibility = View.VISIBLE
                    } else {
                        showGenericConfirmation()
                    }
                } else {
                    Log.e(TAG, "Error loading order: ${result.exceptionOrNull()?.message}")
                    showGenericConfirmation()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading order: ${e.message}", e)
                showGenericConfirmation()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun showGenericConfirmation() {
        // Hide order-specific info
        binding.cardOrderInfo.visibility = View.GONE
        
        // Show generic confirmation message
        binding.tvThankYou.text = "Thank You for Your Order!"
        binding.tvConfirmationMessage.text = "Your order has been placed successfully. You will receive a confirmation email shortly."
    }
} 