package com.tiruvear.textiles.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    private var onItemClickListener: ((Product) -> Unit)? = null
    private var onAddToCartClickListener: ((Product) -> Unit)? = null

    fun setOnItemClickListener(listener: (Product) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnAddToCartClickListener(listener: (Product) -> Unit) {
        onAddToCartClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(getItem(position))
                }
            }

            binding.btnAddToCart.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAddToCartClickListener?.invoke(getItem(position))
                }
            }
        }

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            
            // Price formatting
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            val formattedPrice = formatter.format(product.salePrice ?: product.basePrice)
            binding.tvProductPrice.text = formattedPrice

            // Original price (if on sale)
            if (product.salePrice != null && product.salePrice < product.basePrice) {
                val formattedOriginalPrice = formatter.format(product.basePrice)
                binding.tvProductOriginalPrice.text = formattedOriginalPrice
                binding.tvProductOriginalPrice.visibility = android.view.View.VISIBLE
            } else {
                binding.tvProductOriginalPrice.visibility = android.view.View.GONE
            }

            // Load image using Glide
            val primaryImage = product.images?.find { it.isPrimary }
            val imageUrl = primaryImage?.imageUrl ?: product.images?.firstOrNull()?.imageUrl
            if (imageUrl != null) {
                Glide.with(binding.ivProduct.context)
                    .load(imageUrl)
                    .centerCrop()
                    .into(binding.ivProduct)
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
} 