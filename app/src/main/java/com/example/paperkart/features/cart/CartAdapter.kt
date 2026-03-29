package com.example.paperkart.features.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.paperkart.R
import com.example.paperkart.core.utils.Constants // ✅ Use Constants for cleaner code
import com.example.paperkart.data.dto.cart.CartItemDto
import com.example.paperkart.databinding.ItemCartBinding

class CartAdapter(
    private val onDeleteClick: (CartItemDto) -> Unit
) : ListAdapter<CartItemDto, CartAdapter.CartViewHolder>(CartDiffCallback) {

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItemDto) {
            // 1. Setup Text Data
            binding.tvName.text = item.product.name
            binding.tvSku.text = "SKU: ${item.sku}"
            binding.tvPrice.text = "₹${item.priceAtTime}"
            binding.tvQuantity.text = "Qty: ${item.quantity}"

            // 2. Build Image URL Safely
            // Extract .url from the ImageDto object within the product
            val rawPath = item.product.coverImage?.url

            val imageUrl = when {
                rawPath.isNullOrBlank() -> null

                // If it's a Cloudinary URL, fix the IP if it's shifted
                rawPath.startsWith("http") -> {
                    rawPath.replace("192.168.0.198", "192.168.0.197")
                }

                // For local storage paths (e.g., "books/image.jpg")
                else -> {
                    val cleanPath = rawPath.removePrefix("/")
                        .removePrefix("uploads/")

                    // Constants.IMAGE_BASE_URL should be "http://192.168.0.197:3000/uploads/"
                    Constants.IMAGE_BASE_URL + cleanPath
                }
            }

            // 3. Load with Glide
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_logo) // Your PaperKart branding
                .error(R.drawable.ic_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.ivProduct)

            // 4. Handle Remove Button
            // Using btnRemove which matches your item_cart.xml layout
            binding.btnRemove.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        object CartDiffCallback : DiffUtil.ItemCallback<CartItemDto>() {
            override fun areItemsTheSame(oldItem: CartItemDto, newItem: CartItemDto): Boolean {
                // Unique check: Product ID + SKU combination
                return oldItem.product.id == newItem.product.id && oldItem.sku == newItem.sku
            }

            override fun areContentsTheSame(oldItem: CartItemDto, newItem: CartItemDto): Boolean {
                // Ensure CartItemDto is a data class for proper structural equality check
                return oldItem == newItem
            }
        }
    }
}