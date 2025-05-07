package com.tiruvear.textiles.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.CartItem
import com.tiruvear.textiles.databinding.ItemCartBinding

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onRemoveItem: (CartItem) -> Unit,
    private val onUpdateQuantity: (CartItem, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    
    fun updateItems(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }
    
    override fun getItemCount(): Int = cartItems.size
    
    inner class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(cartItem: CartItem) {
            val product = cartItem.product
            
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "₹${String.format("%.2f", cartItem.price)}"
            binding.tvQuantity.text = cartItem.quantity.toString()
            
            // Load product image
            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(binding.ivProduct)
            
            // Set up click listeners
            binding.ivRemove.setOnClickListener {
                onRemoveItem(cartItem)
            }
            
            binding.btnDecrease.setOnClickListener {
                val newQuantity = cartItem.quantity - 1
                if (newQuantity > 0) {
                    onUpdateQuantity(cartItem, newQuantity)
                } else {
                    onRemoveItem(cartItem)
                }
            }
            
            binding.btnIncrease.setOnClickListener {
                val newQuantity = cartItem.quantity + 1
                onUpdateQuantity(cartItem, newQuantity)
            }
        }
    }
} 