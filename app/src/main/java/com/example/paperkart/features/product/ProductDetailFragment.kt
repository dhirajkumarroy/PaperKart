package com.example.paperkart.features.product

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
        private const val BASE_URL = "http://192.168.0.198:3000/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSystemUI()

        session = SessionManager(this)
        val api = RetrofitClient.getInstance(this).create(ProductApi::class.java)
        repo = ProductRepository(api)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val productId = intent.getStringExtra("productId")
        if (productId == null) {
            Toast.makeText(this, "Product ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadProduct(productId)
    }

    private fun setupSystemUI() {
        // 1. Enable full screen drawing
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        window.statusBarColor = Color.TRANSPARENT

        // 2. Fix: Add padding to the AppBar so it's not hidden under the status bar (time/notifications)
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
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
        
        binding.tvRating.text = "${item.ratings?.average ?: 0.0} (${item.ratings?.count ?: 0} reviews)"

        val imageUrl = when {
            item.coverImage.isNullOrBlank() -> null
            item.coverImage.startsWith("http") -> item.coverImage
            else -> BASE_URL + item.coverImage.removePrefix("/")
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_logo)
            .error(R.drawable.ic_error)
            .into(binding.ivProduct)
    }

    private fun setupAddToCart(product: ProductDto) {
        binding.btnAddToCart.setOnClickListener {
            if (session.getToken() == null) {
                Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val selectedVariant = product.variants?.firstOrNull { it.stock > 0 }
            
            if (selectedVariant == null) {
                Toast.makeText(this, "Sorry, this product is out of stock", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = binding.tvQuantity.text.toString().toIntOrNull() ?: 1
            addToCartLogic(product.id, "default-sku", quantity)
        }

        binding.btnPlus.setOnClickListener {
            val current = binding.tvQuantity.text.toString().toIntOrNull() ?: 1
            binding.tvQuantity.text = (current + 1).toString()
        }

        binding.btnMinus.setOnClickListener {
            val current = binding.tvQuantity.text.toString().toIntOrNull() ?: 1
            if (current > 1) {
                binding.tvQuantity.text = (current - 1).toString()
            }
        }
    }

    private fun addToCartLogic(productId: String, sku: String, quantity: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cartApi = RetrofitClient.getInstance(this@ProductDetailActivity).create(CartApi::class.java)
                val cartRepo = CartRepository(cartApi)

                val response = cartRepo.addToCart(productId, sku, quantity)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductDetailActivity, "Added to Cart! 🛒", Toast.LENGTH_SHORT).show()
                    } else {
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
