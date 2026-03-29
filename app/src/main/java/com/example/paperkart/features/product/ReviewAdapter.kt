package com.example.paperkart.features.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.paperkart.data.api.ReviewDto
import com.example.paperkart.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(private var reviews: List<ReviewDto>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]
        holder.binding.tvUserName.text = review.user.name
        holder.binding.tvComment.text = review.comment
        holder.binding.ratingBar.rating = review.rating.toFloat()
        
        // Simple date formatting
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val date = inputFormat.parse(review.createdAt)
            holder.binding.tvDate.text = if (date != null) outputFormat.format(date) else review.createdAt
        } catch (e: Exception) {
            holder.binding.tvDate.text = review.createdAt
        }
    }

    override fun getItemCount() = reviews.size

    fun updateData(newReviews: List<ReviewDto>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
