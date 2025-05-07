package com.tiruvear.textiles.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.Banner

/**
 * Adapter for displaying promotional banners in a ViewPager2
 */
class BannerAdapter(private val banners: List<Banner>) : 
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(banners[position])
    }
    
    override fun getItemCount(): Int = banners.size
    
    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_banner)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_banner_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tv_banner_description)
        
        fun bind(banner: Banner) {
            titleTextView.text = banner.title
            descriptionTextView.text = banner.description
            
            Glide.with(itemView.context)
                .load(banner.imageResId)
                .centerCrop()
                .into(imageView)
        }
    }
} 