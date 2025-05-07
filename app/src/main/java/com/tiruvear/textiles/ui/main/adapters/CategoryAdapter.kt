package com.tiruvear.textiles.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.ProductCategory
import com.tiruvear.textiles.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<ProductCategory>,
    private val onCategoryClicked: (ProductCategory) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClicked(categories[position])
                }
            }
        }

        fun bind(category: ProductCategory) {
            binding.tvCategoryName.text = category.name
            
            // Load image with Glide
            Glide.with(binding.root.context)
                .load(category.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.placeholder_image) 
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(binding.ivCategory)
        }
    }
} 