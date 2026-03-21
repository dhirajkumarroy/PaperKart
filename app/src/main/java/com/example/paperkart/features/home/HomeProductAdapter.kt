package com.example.paperkart.features.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.paperkart.R
import com.example.paperkart.core.utils.Constants
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

            val imageUrl = when {
                product.coverImage.isNullOrBlank() -> null
                product.coverImage.startsWith("http") -> product.coverImage
                else -> {
                    val cleanPath = product.coverImage.removePrefix("/")
                    Constants.IMAGE_BASE_URL + cleanPath
                }
            }

            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.ivImage)

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
        val DiffCallback = object : DiffUtil.ItemCallback<ProductDto>() {
            override fun areItemsTheSame(oldItem: ProductDto, newItem: ProductDto) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductDto, newItem: ProductDto) =
                oldItem == newItem
        }
    }
}
