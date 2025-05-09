package com.tiruvear.textiles.ui.main.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.OrderItem
import com.tiruvear.textiles.databinding.ItemOrderProductBinding

class OrderItemAdapter(
    private val items: List<OrderItem>,
    private val onItemClick: ((OrderItem) -> Unit)? = null
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderItemViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
    
    inner class OrderItemViewHolder(private val binding: ItemOrderProductBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: OrderItem) {
            val product = item.product
            val context = binding.root.context
            
            // Set product details
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "₹${String.format("%.2f", item.price)}"
            binding.tvQuantity.text = "Qty: ${item.quantity}"
            binding.tvTotalPrice.text = "₹${String.format("%.2f", item.price * item.quantity)}"
            
            // Load product image if available
            val productImage = product.imageUrl
            if (productImage != null) {
                Glide.with(context)
                    .load(productImage)
                    .apply(RequestOptions()
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder))
                    .into(binding.ivProductImage)
            } else {
                Glide.with(context)
                    .load(R.drawable.ic_product_placeholder)
                    .into(binding.ivProductImage)
            }
            
            // Show discount if available
            if (product.salePrice != null && product.salePrice < product.basePrice) {
                binding.tvOriginalPrice.text = "₹${String.format("%.2f", product.basePrice)}"
                binding.tvOriginalPrice.visibility = View.VISIBLE
                binding.tvOriginalPrice.paintFlags = binding.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                
                // Calculate discount percentage
                try {
                    val discountPercentage = ((product.basePrice - product.salePrice) / product.basePrice * 100).toInt()
                    if (discountPercentage > 0 && binding.root.findViewById<View>(R.id.tv_discount) != null) {
                        binding.tvDiscount.text = "$discountPercentage% OFF"
                        binding.tvDiscount.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    // Ignore if tv_discount doesn't exist
                }
            } else {
                binding.tvOriginalPrice.visibility = View.GONE
                try {
                    if (binding.root.findViewById<View>(R.id.tv_discount) != null) {
                        binding.tvDiscount.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    // Ignore if tv_discount doesn't exist
                }
            }
            
            // Set click listener
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
} 