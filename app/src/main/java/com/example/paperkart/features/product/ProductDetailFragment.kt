package com.example.paperkart.features.product

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.CartApi
import com.example.paperkart.data.api.ProductApi
import com.example.paperkart.data.dto.product.ProductDto
import com.example.paperkart.databinding.ActivityProductDetailBinding
import com.example.paperkart.features.cart.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var repo: ProductRepository
    private lateinit var session: SessionManager

    companion object {
        // Change this to match your local Node.js server IP
        private const val BASE_URL = "http://192.168.0.198:3000/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup View Binding
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Production Look: White Status Bar + Black Icons
        setupSystemUI()

        // 3. Initialize Helpers
        session = SessionManager(this)
        val api = RetrofitClient.getInstance(this).create(ProductApi::class.java)
        repo = ProductRepository(api)

        // 4. Handle Navigation
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 5. Get Product Data
        val productId = intent.getStringExtra("productId")
        if (productId == null) {
            Toast.makeText(this, "Product ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadProduct(productId)
    }

    private fun setupSystemUI() {
        // Pushes content behind status bar but makes icons visible
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun loadProduct(productId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val product = repo.getProductById(productId)
                withContext(Dispatchers.Main) {
                    product?.let { item ->
                        displayProductDetails(item)
                        setupAddToCart(item)
                    } ?: run {
                        Toast.makeText(this@ProductDetailActivity, "Product not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductDetailActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayProductDetails(item: ProductDto) {
        binding.tvName.text = item.name
        binding.tvPrice.text = "₹${item.minPrice}"
        binding.tvDescription.text = item.description ?: "No description available"

        val imageUrl = when {
            item.coverImage.isNullOrBlank() -> null
            item.coverImage.startsWith("http") -> item.coverImage
            else -> BASE_URL + item.coverImage.removePrefix("/")
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_logo) // Use your app logo as placeholder
            .error(R.drawable.ic_error)
            .into(binding.ivProduct)
    }

    private fun setupAddToCart(product: ProductDto) {
        binding.btnAddToCart.setOnClickListener {
            // ✅ CHECK 1: Session Check (Prevents 401 Error)
            if (session.getToken() == null) {
                Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // ✅ CHECK 2: Stock Check
            val selectedVariant = product.variants?.firstOrNull { it.stock > 0 }
            val selectedSku = selectedVariant?.sku

            if (selectedSku == null) {
                Toast.makeText(this, "Sorry, this product is out of stock", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Execute Add to Cart
            addToCartLogic(product.id, selectedSku)
        }
    }

    private fun addToCartLogic(productId: String, sku: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cartApi = RetrofitClient.getInstance(this@ProductDetailActivity).create(CartApi::class.java)
                val cartRepo = CartRepository(cartApi)

                val response = cartRepo.addToCart(productId, sku, 1)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductDetailActivity, "Added to Cart! 🛒", Toast.LENGTH_SHORT).show()
                    } else {
                        // Handle 401 or other API errors
                        val errorMsg = if (response.code() == 401) "Session expired. Please re-login." else "Failed to add"
                        Toast.makeText(this@ProductDetailActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductDetailActivity, "Server connection failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}