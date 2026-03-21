package com.example.paperkart.features.order

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.data.api.OrderApi
import com.example.paperkart.databinding.FragmentOrderBinding

class OrderFragment : Fragment(R.layout.fragment_order) {

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: OrderViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOrderBinding.bind(view)

        // Initialize API and ViewModel
        val api = RetrofitClient.getInstance(requireContext()).create(OrderApi::class.java)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return OrderViewModel(api) as T
            }
        })[OrderViewModel::class.java]

        setupRecyclerView()
        observeData()

        // Fetch data
        viewModel.fetchMyOrders()
    }

    private fun setupRecyclerView() {
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeData() {
        viewModel.userOrders.observe(viewLifecycleOwner) { orders ->
            binding.progressBar.isVisible = false
            val isEmpty = orders.isNullOrEmpty()
            binding.layoutEmpty.isVisible = isEmpty
            binding.rvOrders.isVisible = !isEmpty

            if (!isEmpty) {
                binding.rvOrders.adapter = OrderAdapter(orders)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}