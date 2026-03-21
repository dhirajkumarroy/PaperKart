package com.example.paperkart.features.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.paperkart.R
import com.example.paperkart.data.dto.cart.CartItemDto
import com.example.paperkart.databinding.ItemCartBinding

class CartAdapter(
    private val onDeleteClick: (CartItemDto) -> Unit
) : ListAdapter<CartItemDto, CartAdapter.CartViewHolder>(CartDiffCallback) {

    companion object {
        private const val BASE_URL = "http://192.168.0.198:3000/"
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItemDto) {
            binding.tvName.text = item.product.name
            binding.tvSku.text = "SKU: ${item.sku}"
            binding.tvPrice.text = "₹${item.priceAtTime}"
            binding.tvQuantity.text = "Qty: ${item.quantity}"

            // Build image URL safely
            val imageUrl = when {
                item.product.coverImage.isNullOrBlank() -> null
                item.product.coverImage.startsWith("http") -> item.product.coverImage
                else -> BASE_URL + item.product.coverImage.removePrefix("/")
            }

            Glide.with(binding.root.context)
                .load(imageUrl ?: R.drawable.ic_logo)
                .placeholder(R.drawable.ic_logo)
                .error(R.drawable.ic_error)
                .centerCrop()
                .into(binding.ivProduct)

            // Delete button logic
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

    object CartDiffCallback : DiffUtil.ItemCallback<CartItemDto>() {
        override fun areItemsTheSame(oldItem: CartItemDto, newItem: CartItemDto): Boolean {
            // In your backend, a unique cart row is defined by Product ID + SKU
            return oldItem.product.id == newItem.product.id && oldItem.sku == newItem.sku
        }

        override fun areContentsTheSame(oldItem: CartItemDto, newItem: CartItemDto): Boolean {
            return oldItem == newItem
        }
    }
}