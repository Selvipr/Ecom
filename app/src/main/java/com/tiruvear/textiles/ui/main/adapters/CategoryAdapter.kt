package com.tiruvear.textiles.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tiruvear.textiles.data.models.ProductCategory
import com.tiruvear.textiles.databinding.ItemCategoryBinding

class CategoryAdapter : ListAdapter<ProductCategory, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var onItemClickListener: ((ProductCategory) -> Unit)? = null

    fun setOnItemClickListener(listener: (ProductCategory) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(getItem(position))
                }
            }
        }

        fun bind(category: ProductCategory) {
            binding.tvCategoryName.text = category.name

            // Load image using Glide
            if (category.imageUrl != null) {
                Glide.with(binding.ivCategory.context)
                    .load(category.imageUrl)
                    .centerCrop()
                    .into(binding.ivCategory)
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<ProductCategory>() {
        override fun areItemsTheSame(oldItem: ProductCategory, newItem: ProductCategory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductCategory, newItem: ProductCategory): Boolean {
            return oldItem == newItem
        }
    }
} 