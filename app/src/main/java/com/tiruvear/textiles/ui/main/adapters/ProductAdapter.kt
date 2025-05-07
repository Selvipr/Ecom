package com.tiruvear.textiles.ui.main.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.Product
import com.tiruvear.textiles.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val products: List<Product>,
    private val onProductClicked: (Product) -> Unit,
    private val onAddToCartClicked: (Product) -> Unit,
    private val onBuyNowClicked: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    init {
        currencyFormat.maximumFractionDigits = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Set click listeners
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProductClicked(products[position])
                }
            }
            
            binding.btnAddToCart.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAddToCartClicked(products[position])
                }
            }
            
            binding.btnBuyNow.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBuyNowClicked(products[position])
                }
            }
        }

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            
            // Format and display prices
            binding.tvProductPrice.text = currencyFormat.format(product.basePrice)
            
            if (product.salePrice != null && product.salePrice < product.basePrice) {
                binding.tvProductSalePrice.apply {
                    text = currencyFormat.format(product.basePrice)
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                binding.tvProductPrice.text = currencyFormat.format(product.salePrice)
            } else {
                binding.tvProductSalePrice.text = ""
            }
            
            // Load product image
            Glide.with(binding.root.context)
                .load(product.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(binding.ivProduct)
        }
    }
} 