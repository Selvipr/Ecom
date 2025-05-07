package com.tiruvear.textiles.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tiruvear.textiles.data.models.Order
import com.tiruvear.textiles.data.models.OrderStatus
import com.tiruvear.textiles.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private val orders: List<Order>,
    private val onTrackOrder: (Order) -> Unit,
    private val onViewDetails: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {
    
    private val dateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }
    
    override fun getItemCount(): Int = orders.size
    
    inner class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(order: Order) {
            binding.tvOrderId.text = order.id
            binding.tvOrderDate.text = dateFormatter.format(order.orderDate)
            binding.tvOrderTotal.text = "₹${String.format("%.2f", order.totalAmount)}"
            
            // Set order status with appropriate color
            binding.tvOrderStatus.text = getFormattedStatus(order.status)
            binding.tvOrderStatus.setTextColor(getStatusColor(order.status))
            
            // Setup buttons
            binding.btnTrackOrder.setOnClickListener {
                onTrackOrder(order)
            }
            
            binding.btnViewDetails.setOnClickListener {
                onViewDetails(order)
            }
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
            val context = binding.root.context
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
    }
} 