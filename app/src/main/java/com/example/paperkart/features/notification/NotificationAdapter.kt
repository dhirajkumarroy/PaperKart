package com.example.paperkart.features.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.paperkart.R
import com.example.paperkart.data.api.NotificationDto
import com.example.paperkart.databinding.ItemProductNotificationBinding

class NotificationAdapter(
    private var items: List<NotificationDto>,
    private val onItemClick: (NotificationDto) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProductNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvTitle.text = item.title
        holder.binding.tvMessage.text = item.message
        holder.binding.tvTime.text = item.createdAt // You might want to format this

        // Set icon based on type
        val iconRes = when (item.type.uppercase()) {
            "ORDER" -> R.drawable.ic_orders
            "PROMO" -> R.drawable.ic_logo
            else -> R.drawable.ic_notification
        }
        holder.binding.ivTypeIcon.setImageResource(iconRes)

        // Show unread dot
        holder.binding.viewUnreadDot.visibility = if (item.read) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<NotificationDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
