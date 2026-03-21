package com.example.paperkart.features.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.ProductApi
import com.example.paperkart.databinding.FragmentHomeBinding
import com.example.paperkart.features.cart.CartFragment
import com.example.paperkart.features.product.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: HomeProductAdapter
    private lateinit var session: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // 1. Initialize Session Manager to get User Data
        session = SessionManager(requireContext())

        // 2. Setup UI and Logic
        setupUserHeader()
        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // 3. Initial Data Load
        viewModel.loadProducts()
    }

    private fun setupUserHeader() {
        // Displays the name saved in SharedPreferences during Login
        val userName = session.getUserName()
        binding.tvUserName.text = if (!userName.isNullOrEmpty()) {
            userName
        } else {
            "PaperKart User"
        }
    }

    private fun setupClickListeners() {
        // ✅ CART ICON: Navigates to the CartFragment
        binding.ivCart.setOnClickListener {
            navigateToCart()
        }

        // ✅ SEARCH CARD: Usually opens a search interface.
        // Currently set to a Toast so it doesn't accidentally open the Cart.
        binding.searchCard.setOnClickListener {
            Toast.makeText(requireContext(), "Search coming soon!", Toast.LENGTH_SHORT).show()
            // In the future, replace this with navigateToSearch()
        }
    }

    private fun navigateToCart() {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, CartFragment())
            .addToBackStack(null) // Allows user to press 'Back' to return home
            .commit()
    }

    private fun setupRecyclerView() {
        adapter = HomeProductAdapter { product ->
            // Opens the Detail Activity for the clicked product
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra("productId", product.id)
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@HomeFragment.adapter
        }

        // Swipe to Refresh logic
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadProducts()
        }
    }

    private fun setupViewModel() {
        val api = RetrofitClient.getInstance(requireContext()).create(ProductApi::class.java)
        val repo = ProductRepository(api)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProductViewModel(repo) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[ProductViewModel::class.java]
    }

    private fun setupObservers() {
        // Update product list when data changes
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            binding.swipeRefresh.isRefreshing = false
        }

        // Show/Hide progress bar
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up to prevent memory leaks
    }
}