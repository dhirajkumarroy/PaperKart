package com.example.paperkart.features.categories

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.data.api.CategoryApi
import com.example.paperkart.databinding.FragmentCategoriesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesFragment : Fragment(R.layout.fragment_categories) {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoriesBinding.bind(view)

        setupRecyclerView()
        setupListeners()
        fetchCategories()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with a click listener
        categoryAdapter = CategoryAdapter { category ->
            // In the next part, we can navigate to a "ProductListFragment"
            // filtered by this category ID/Slug
            Toast.makeText(requireContext(), "Clicked: ${category.name}", Toast.LENGTH_SHORT).show()
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupListeners() {
        // If you decide to add SwipeRefreshLayout to your fragment_categories.xml
        // binding.swipeRefresh.setOnRefreshListener { fetchCategories() }
    }

    private fun fetchCategories() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitClient.getInstance(requireContext()).create(CategoryApi::class.java)
                val response = api.getCategories()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val categories = response.body() ?: emptyList()
                        categoryAdapter.submitList(categories)
                    } else {
                        showError("Failed to load categories")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Check your internet connection")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    // binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun showError(message: String) {
        if (isAdded) { // Ensures fragment is still attached to activity
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ✅ Prevent Memory Leaks
    }
}