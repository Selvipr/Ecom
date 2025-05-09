package com.tiruvear.textiles.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
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
    private val onViewDetails: (Order) -> Unit,
    private val onCancelOrder: (Order) -> Unit
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
            
            // Show delivery date if available and delivered
            if (order.deliveryDate != null && order.status == OrderStatus.DELIVERED) {
                binding.tvDeliveryDate.text = "Delivered on ${dateFormatter.format(order.deliveryDate)}"
                binding.tvDeliveryDate.visibility = View.VISIBLE
            } else if (order.status == OrderStatus.CANCELLED) {
                binding.tvDeliveryDate.text = "Order was cancelled"
                binding.tvDeliveryDate.visibility = View.VISIBLE
            } else if (order.status == OrderStatus.RETURNED) {
                binding.tvDeliveryDate.text = "Order was returned"
                binding.tvDeliveryDate.visibility = View.VISIBLE
            } else {
                binding.tvDeliveryDate.visibility = View.GONE
            }
            
            // Show number of items
            val itemCount = order.items.size
            val itemText = if (itemCount == 1) "1 item" else "$itemCount items"
            binding.tvItemCount.text = itemText
            
            // Setup buttons based on order status
            setupButtons(order)
            
            // Make the entire order item clickable to view details
            binding.root.setOnClickListener {
                onViewDetails(order)
            }
        }
        
        private fun setupButtons(order: Order) {
            // Track Order button only visible for shipped orders
            if (order.status == OrderStatus.SHIPPED) {
                binding.btnTrackOrder.visibility = View.VISIBLE
                binding.btnTrackOrder.setOnClickListener {
                    onTrackOrder(order)
                }
            } else {
                binding.btnTrackOrder.visibility = View.GONE
            }
            
            // View Details button always visible
            binding.btnViewDetails.setOnClickListener {
                onViewDetails(order)
            }
            
            // Cancel button only for pending or confirmed orders
            if (order.status == OrderStatus.PENDING || order.status == OrderStatus.CONFIRMED) {
                binding.btnCancelOrder.visibility = View.VISIBLE
                binding.btnCancelOrder.setOnClickListener {
                    onCancelOrder(order)
                }
            } else {
                binding.btnCancelOrder.visibility = View.GONE
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