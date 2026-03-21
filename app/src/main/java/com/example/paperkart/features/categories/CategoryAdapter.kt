package com.example.paperkart.features.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.paperkart.data.dto.category.CategoryDto
import com.example.paperkart.databinding.ItemCategoryBinding

class CategoryAdapter(private val onCategoryClick: (CategoryDto) -> Unit) :
    ListAdapter<CategoryDto, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryDto) {
            binding.tvCategoryName.text = category.name

            // Show "12 Products" or "0 Products"
            binding.tvProductCount.text = "${category.productCount} Products"

            // Handle Click
            binding.root.setOnClickListener { onCategoryClick(category) }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryDto>() {
        override fun areItemsTheSame(oldItem: CategoryDto, newItem: CategoryDto) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CategoryDto, newItem: CategoryDto) = oldItem == newItem
    }
}