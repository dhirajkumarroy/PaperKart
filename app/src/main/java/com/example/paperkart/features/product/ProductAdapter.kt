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
        // ✅ Current Server Base URL
        private const val BASE_URL = "http://192.168.0.197:3000/"

        // Dynamic IP Fixes for mixed environments
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

        // 2. Extract and Fix the Image URL
        // Now extracting .url from the ImageDto object
        val rawPath = item.coverImage?.url

        val imageUrl = when {
            rawPath.isNullOrBlank() -> null

            // If it's a full Cloudinary/External URL
            rawPath.startsWith("http") -> {
                rawPath.replace(OLD_IP, CURRENT_IP)
            }

            // If it's a local path (e.g., "books/image.jpg" or "uploads/books/image.jpg")
            else -> {
                // Remove redundant prefixes to avoid double slashes or triple paths
                val cleanPath = rawPath.removePrefix("/")
                    .removePrefix("uploads/")

                // Construct: http://192.168.0.197:3000/uploads/books/...
                "${BASE_URL}uploads/$cleanPath"
            }
        }

        // 3. Load with Glide
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_cart)
            .error(R.drawable.ic_cart)
            .centerCrop()
            .into(holder.binding.ivImage)

        // 4. Handle Clicks (uses _id from DTO)
        holder.itemView.setOnClickListener {
            onClick(item.id)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<ProductDto>) {
        list = newList
        notifyDataSetChanged()
    }
}