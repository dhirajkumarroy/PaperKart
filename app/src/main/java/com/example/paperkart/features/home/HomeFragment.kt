package com.example.paperkart.features.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.NotificationApi
import com.example.paperkart.data.api.ProductApi
import com.example.paperkart.databinding.FragmentHomeBinding
import com.example.paperkart.features.cart.CartFragment
import com.example.paperkart.features.notification.NotificationActivity
import com.example.paperkart.features.notification.NotificationViewModel
import com.example.paperkart.features.product.*
import com.example.paperkart.features.watchlist.WatchlistActivity

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProductViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var adapter: HomeProductAdapter
    private lateinit var session: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        session = SessionManager(requireContext())

        // 1. Reset Status Bar to match other fragments (My Order, Profile)
        setupSystemUI()

        // 2. UI Setup
        setupUserHeader()
        setupViewModels()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // 3. Data Fetching
        viewModel.loadProducts()
        notificationViewModel.fetchUnreadCount()
    }

    private fun setupSystemUI() {
        val window = requireActivity().window
        
        // Return to standard behavior so it matches Order and Profile fragments
        WindowCompat.setDecorFitsSystemWindows(window, true)
        
        // Set standard white status bar with dark icons
        window.statusBarColor = Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
        }

        // Remove the manual padding listener that was causing "Double Padding"
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout, null)
    }

    private fun setupUserHeader() {
        val userName = session.getUserName()
        binding.tvUserName.text = if (!userName.isNullOrEmpty()) userName else "PaperKart User"

        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when (hour) {
            in 0..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
    }

    private fun setupClickListeners() {
        binding.btnWatchlist.setOnClickListener {
            startActivity(Intent(requireContext(), WatchlistActivity::class.java))
        }

        binding.btnNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }

        binding.btnCart.setOnClickListener {
            navigateToCart()
        }

        binding.cardSearch.setOnClickListener {
            Toast.makeText(requireContext(), "Search functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToCart() {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentContainer, CartFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun setupViewModels() {
        val productApi = RetrofitClient.getInstance(requireContext()).create(ProductApi::class.java)
        val productRepo = ProductRepository(productApi)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(productRepo) as T
            }
        })[ProductViewModel::class.java]

        val notifApi = RetrofitClient.getInstance(requireContext()).create(NotificationApi::class.java)
        notificationViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return NotificationViewModel(notifApi) as T
            }
        })[NotificationViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = HomeProductAdapter { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra("productId", product.id)
            startActivity(intent)
        }

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@HomeFragment.adapter
            isNestedScrollingEnabled = false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadProducts()
            notificationViewModel.fetchUnreadCount()
        }
    }

    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        notificationViewModel.unreadCount.observe(viewLifecycleOwner) { count ->
            // Update UI if needed
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        notificationViewModel.fetchUnreadCount()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
