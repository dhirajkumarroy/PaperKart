package com.example.paperkart.features.product

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.data.api.ProductApi
import com.example.paperkart.databinding.ActivityProductBinding

class ProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val api = RetrofitClient.getInstance(this).create(ProductApi::class.java)
        val repo = ProductRepository(api)
        viewModel = ProductViewModel(repo)

        // ✅ FIXED: Now starts ProductDetailActivity on click
        adapter = ProductAdapter(emptyList()) { productId ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("productId", productId)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.products.observe(this) {
            adapter.updateData(it)
        }

        viewModel.loadProducts()
    }
}