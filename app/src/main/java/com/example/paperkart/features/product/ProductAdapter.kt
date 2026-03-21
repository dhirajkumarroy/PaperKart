package com.example.paperkart.features.product

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
        // FIX: Added trailing slash
        private const val BASE_URL = "http://192.168.0.198:3000/"
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

        holder.binding.tvName.text = item.name
        holder.binding.tvPrice.text = "₹${item.minPrice}"

        // FIX: Safe URL construction
        val imageUrl = when {
            item.coverImage.isNullOrBlank() -> null
            item.coverImage.startsWith("http") -> item.coverImage
            else -> {
                val cleanPath = if (item.coverImage.startsWith("/"))
                    item.coverImage.substring(1) else item.coverImage
                BASE_URL + cleanPath
            }
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl ?: R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.binding.ivImage)

        // Trigger the click lambda with ID
        holder.itemView.setOnClickListener {
            onClick(item.id)
        }
    }

    fun updateData(newList: List<ProductDto>) {
        list = newList
        notifyDataSetChanged()
    }
}