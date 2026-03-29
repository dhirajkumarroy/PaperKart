package com.example.paperkart.features.product

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.core.utils.Constants
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.CartApi
import com.example.paperkart.data.api.ProductApi
import com.example.paperkart.data.api.ReviewApi
import com.example.paperkart.data.api.WatchlistApi
import com.example.paperkart.data.api.CreateReviewRequest
import com.example.paperkart.data.dto.product.ProductDto
import com.example.paperkart.databinding.ActivityProductDetailBinding
import com.example.paperkart.databinding.DialogAddReviewBinding
import com.example.paperkart.features.cart.CartRepository
import com.example.paperkart.features.order.CheckoutActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var repo: ProductRepository
    private lateinit var session: SessionManager
    private lateinit var reviewAdapter: ReviewAdapter
    private var isFavorite: Boolean = false
    private var currentProductId: String? = null

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

        currentProductId = intent.getStringExtra("productId")
        if (currentProductId == null) {
            Toast.makeText(this, "Product ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupReviewList()
        loadProduct(currentProductId!!)
        checkWatchlistStatus(currentProductId!!)
        loadReviews(currentProductId!!)
    }

    private fun setupSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        window.statusBarColor = Color.TRANSPARENT

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupReviewList() {
        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(this@ProductDetailActivity)
            adapter = reviewAdapter
        }

        binding.btnAddReview.setOnClickListener {
            showAddReviewDialog()
        }
    }

    private fun loadProduct(productId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val product = repo.getProductById(productId)
                withContext(Dispatchers.Main) {
                    product?.let { item ->
                        displayProductDetails(item)
                        setupActions(item)
                    }
                }
            } catch (e: Exception) { /* Handle error */ }
        }
    }

    private fun loadReviews(productId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val reviewApi = RetrofitClient.getInstance(this@ProductDetailActivity).create(ReviewApi::class.java)
                val response = reviewApi.getProductReviews(productId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val reviews = response.body() ?: emptyList()
                        reviewAdapter.updateData(reviews)
                        binding.tvNoReviews.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            } catch (e: Exception) { /* Handle error */ }
        }
    }

    private fun showAddReviewDialog() {
        if (session.getToken() == null) {
            Toast.makeText(this, "Login to review", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSubmit.setOnClickListener {
            val rating = dialogBinding.ratingBar.rating.toInt()
            val comment = dialogBinding.etComment.text.toString()

            if (rating == 0) {
                Toast.makeText(this, "Select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitReview(rating, comment, dialog)
        }

        dialog.show()
    }

    private fun submitReview(rating: Int, comment: String, dialog: AlertDialog) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val reviewApi = RetrofitClient.getInstance(this@ProductDetailActivity).create(ReviewApi::class.java)
                val response = reviewApi.addReview(currentProductId!!, CreateReviewRequest(rating, comment))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProductDetailActivity, "Review added!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadReviews(currentProductId!!) // Refresh list
                    } else {
                        val error = response.errorBody()?.string() ?: "Failed to add review"
                        Toast.makeText(this@ProductDetailActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProductDetailActivity, "Server Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkWatchlistStatus(productId: String) {
        if (session.getToken() == null) return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val watchlistApi = RetrofitClient.getInstance(this@ProductDetailActivity).create(WatchlistApi::class.java)
                val response = watchlistApi.checkWatchlist(productId)
                if (response.isSuccessful) {
                    isFavorite = response.body()?.watched ?: false
                    withContext(Dispatchers.Main) {
                        binding.fabWishlist.setImageResource(if (isFavorite) R.drawable.ic_heart else R.drawable.ic_heart_outline)
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun displayProductDetails(item: ProductDto) {
        binding.tvName.text = item.name
        binding.tvPrice.text = "₹${item.minPrice}"
        binding.tvDescription.text = item.description ?: "No description available"
        binding.tvRating.text = "${item.ratings?.average ?: 0.0} (${item.ratings?.count ?: 0} reviews)"

        val rawPath = item.coverImage?.url
        val imageUrl = when {
            rawPath.isNullOrBlank() -> null
            rawPath.startsWith("http") -> rawPath.replace("192.168.0.198", "192.168.0.197")
            else -> Constants.IMAGE_BASE_URL + rawPath.removePrefix("/").removePrefix("uploads/")
        }

        Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_logo).into(binding.ivProduct)
    }

    private fun setupActions(product: ProductDto) {
        binding.fabWishlist.setOnClickListener { toggleWatchlist(product.id) }
        binding.btnAddToCart.setOnClickListener { addToCart(product, false) }
        binding.btnBuyNow.setOnClickListener { addToCart(product, true) }
        
        binding.btnPlus.setOnClickListener {
            val current = binding.tvQuantity.text.toString().toIntOrNull() ?: 1
            binding.tvQuantity.text = (current + 1).toString()
        }
        binding.btnMinus.setOnClickListener {
            val current = binding.tvQuantity.text.toString().toIntOrNull() ?: 1
            if (current > 1) binding.tvQuantity.text = (current - 1).toString()
        }
    }

    private fun toggleWatchlist(productId: String) {
        if (session.getToken() == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val watchlistApi = RetrofitClient.getInstance(this@ProductDetailActivity).create(WatchlistApi::class.java)
                val response = if (isFavorite) watchlistApi.removeFromWatchlist(productId) else watchlistApi.addToWatchlist(mapOf("productId" to productId))
                if (response.isSuccessful) {
                    isFavorite = !isFavorite
                    withContext(Dispatchers.Main) {
                        binding.fabWishlist.setImageResource(if (isFavorite) R.drawable.ic_heart else R.drawable.ic_heart_outline)
                        Toast.makeText(this@ProductDetailActivity, if (isFavorite) "Added to Watchlist" else "Removed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun addToCart(product: ProductDto, goToCheckout: Boolean) {
        if (session.getToken() == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show()
            return
        }
        val variant = product.variants?.firstOrNull { it.stock > 0 } ?: return
        val qty = binding.tvQuantity.text.toString().toIntOrNull() ?: 1
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cartApi = RetrofitClient.getInstance(this@ProductDetailActivity).create(CartApi::class.java)
                val response = CartRepository(cartApi).addToCart(product.id, variant.sku, qty)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        if (goToCheckout) startActivity(Intent(this@ProductDetailActivity, CheckoutActivity::class.java))
                        else Toast.makeText(this@ProductDetailActivity, "Added to Cart!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) { }
        }
    }
}
