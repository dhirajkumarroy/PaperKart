package com.example.paperkart.features.watchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.paperkart.R
import com.example.paperkart.core.utils.Constants
import com.example.paperkart.data.api.WatchlistItemDto
import com.example.paperkart.databinding.ItemProductCardBinding

class WatchlistAdapter(
    private var items: List<WatchlistItemDto>,
    private val onItemClick: (String) -> Unit,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<WatchlistAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProductCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        // Safe check for null product to prevent crash
        val product = item.product ?: return

        holder.binding.tvName.text = product.name
        holder.binding.tvPrice.text = "₹${product.minPrice}"

        val rawPath = product.coverImage?.url
        val imageUrl = if (rawPath?.startsWith("http") == true) {
            rawPath.replace("192.168.0.198", "192.168.0.197")
        } else {
            Constants.IMAGE_BASE_URL + (rawPath?.removePrefix("/")?.removePrefix("uploads/") ?: "")
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_logo)
            .into(holder.binding.ivCover)

        holder.itemView.setOnClickListener { onItemClick(product.id) }
        
        holder.binding.btnWishlist.setImageResource(R.drawable.ic_heart)
        holder.binding.btnWishlist.setOnClickListener {
            onRemoveClick(product.id)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<WatchlistItemDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
