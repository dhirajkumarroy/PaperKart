package com.example.paperkart.features.notification

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.data.api.NotificationApi
import com.example.paperkart.databinding.ActivityOrderListBinding // Reusing a list layout if specialized one doesn't exist, or use fragment_notifications.xml
import com.example.paperkart.databinding.FragmentNotificationsBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var viewModel: NotificationViewModel
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupObservers()

        viewModel.fetchNotifications()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        binding.btnReadAll.setOnClickListener {
            viewModel.markAllRead()
        }
    }

    private fun setupViewModel() {
        val api = RetrofitClient.getInstance(this).create(NotificationApi::class.java)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return NotificationViewModel(api) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList()) { notification ->
            if (!notification.read) {
                viewModel.markAsRead(notification.id)
            }
            // Handle navigation based on notification.type (e.g. open Order details)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchNotifications()
        }
    }

    private fun setupObservers() {
        viewModel.notifications.observe(this) { list ->
            adapter.updateData(list)
            binding.swipeRefresh.isRefreshing = false
            binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
}
