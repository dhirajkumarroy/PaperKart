package com.example.paperkart.features.order

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paperkart.MainActivity
import com.example.paperkart.databinding.ActivityOrderSuccessBinding

class OrderSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Get Order ID from Intent
        val orderId = intent.getStringExtra("EXTRA_ORDER_ID") ?: "Unknown"
        binding.tvOrderId.text = "Order ID: #$orderId"

        // 2. Button Logic: View My Orders
        binding.btnViewOrders.setOnClickListener {
            // Navigate to your Order List Feature (to see status: PLACED, SHIPPED etc)
            val intent = Intent(this, OrderListActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 3. Button Logic: Continue Shopping
        binding.btnContinueShopping.setOnClickListener {
            // Clear all previous activities and go to Main Home
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // Prevent user from going back to Checkout/Cart via back button
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}