package com.example.paperkart.features.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.paperkart.R
import com.example.paperkart.data.dto.order.OrderDto
import com.example.paperkart.databinding.ItemOrderBinding

class OrderAdapter(private val orders: List<OrderDto>) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount() = orders.size

    class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: OrderDto) {
            binding.tvOrderId.text = "Order #${order.id.takeLast(6).uppercase()}"
            binding.tvTotalAmount.text = "₹${order.totalAmount}"
            binding.tvOrderDate.text = "Placed on ${order.createdAt.split("T")[0]}"
            binding.tvStatus.text = order.status

            // Status Styling
            val context = binding.root.context
            when (order.status) {
                "DELIVERED" -> {
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_green) // Create simple shapes for backgrounds
                }
                "CANCELLED" -> {
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                }
                "SHIPPED" -> {
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                }
                else -> {
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                }
            }
        }
    }
}