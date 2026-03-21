package com.example.paperkart.features.cart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.data.api.CartApi
import com.example.paperkart.databinding.FragmentCartBinding
import com.example.paperkart.features.order.CheckoutActivity

class CartFragment : Fragment(R.layout.fragment_cart) {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CartViewModel
    private lateinit var adapter: CartAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCartBinding.bind(view)

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadCart()
    }

    private fun setupViewModel() {
        val api = RetrofitClient.getInstance(requireContext()).create(CartApi::class.java)
        val repo = CartRepository(api)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CartViewModel(repo) as T
            }
        })[CartViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter { item ->
            // Triggered when user clicks delete/remove icon
            viewModel.removeItem(item.product.id, item.sku)
        }
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CartFragment.adapter
        }
    }

    private fun setupObservers() {
        // Observe Cart Items
        viewModel.cart.observe(viewLifecycleOwner) { cart ->
            val hasItems = !cart?.items.isNullOrEmpty()

            // Toggle visibility between List and Empty State
            binding.rvCart.isVisible = hasItems
            binding.layoutEmpty.isVisible = !hasItems
            binding.cardCheckout.isVisible = hasItems // Hide total/button if empty

            if (hasItems) {
                adapter.submitList(cart!!.items)
            }
        }

        // Observe Total Price
        viewModel.totalPrice.observe(viewLifecycleOwner) { price ->
            binding.tvTotal.text = "₹$price"
        }

        // Observe Loading State
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.btnCheckout.isEnabled = !loading
        }

        // Observe Errors
        viewModel.error.observe(viewLifecycleOwner) { err ->
            if (err != null) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnCheckout.setOnClickListener {
            // Ensure cart isn't empty before moving to Checkout
            if (!viewModel.cart.value?.items.isNullOrEmpty()) {
                val intent = Intent(requireContext(), CheckoutActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Optional: Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadCart()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}