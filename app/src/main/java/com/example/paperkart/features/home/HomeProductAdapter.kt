package com.example.paperkart.features.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.paperkart.R
import com.example.paperkart.data.dto.product.ProductDto
import com.example.paperkart.databinding.ItemProductBinding

class HomeProductAdapter(
    private val onProductClick: (ProductDto) -> Unit
) : ListAdapter<ProductDto, HomeProductAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductDto) {
            binding.tvName.text = product.name
            binding.tvPrice.text = "₹${product.minPrice}"
            binding.tvRating.text = product.ratings?.average?.toString() ?: "0.0"

            // FIX: Added trailing slash and path sanitization
            val imageUrl = when {
                product.coverImage.isNullOrBlank() -> null
                product.coverImage.startsWith("http") -> product.coverImage
                else -> {
                    val cleanPath = if (product.coverImage.startsWith("/"))
                        product.coverImage.substring(1) else product.coverImage
                    BASE_URL + cleanPath
                }
            }

            Glide.with(binding.root.context)
                .load(imageUrl ?: R.drawable.ic_logo)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.ivImage)

            // Trigger the click lambda
            binding.root.setOnClickListener {
                onProductClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        // FIX: Added the missing '/' at the end
        private const val BASE_URL = "http://192.168.0.198:3000/"

        val DiffCallback = object : DiffUtil.ItemCallback<ProductDto>() {
            override fun areItemsTheSame(oldItem: ProductDto, newItem: ProductDto) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductDto, newItem: ProductDto) =
                oldItem == newItem
        }
    }
}