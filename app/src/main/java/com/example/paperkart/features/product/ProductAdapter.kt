package com.example.paperkart.features.product

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.paperkart.R
import com.example.paperkart.data.dto.product.ProductDto
import com.example.paperkart.databinding.ItemProductBinding

class ProductAdapter(
    private var list: List<ProductDto>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    companion object {
        // ✅ Updated to your current Mac IP
        private const val BASE_URL = "http://192.168.0.197:3000/"

        // The "Ghost" IP that causes the timeout
        private const val OLD_IP = "192.168.0.198"
        private const val CURRENT_IP = "192.168.0.197"
    }

    inner class VH(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        // 1. Set Text Data
        holder.binding.tvName.text = item.name
        holder.binding.tvPrice.text = "₹${item.minPrice}"

        // 2. Build and Fix the Image URL
        val imageUrl = when {
            item.coverImage.isNullOrBlank() -> null

            // If the URL is absolute (starts with http)
            item.coverImage.startsWith("http") -> {
                // ✅ DYNAMIC FIX: Swap the old IP for the new one if found
                item.coverImage.replace(OLD_IP, CURRENT_IP)
            }

            // If the URL is relative (e.g., "uploads/img.jpg")
            else -> {
                val cleanPath = item.coverImage.removePrefix("/")
                BASE_URL + cleanPath
            }
        }

        // 3. Load with Glide
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_cart) // Your custom PaperKart logo
            .error(R.drawable.ic_cart)      // Fallback if network fails
            .centerCrop()
            .into(holder.binding.ivImage)

        // 4. Handle Clicks (Ensure this matches your DTO field: id vs _id)
        holder.itemView.setOnClickListener {
            item.id?.let { id -> onClick(id) }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<ProductDto>) {
        list = newList
        notifyDataSetChanged()
    }
}