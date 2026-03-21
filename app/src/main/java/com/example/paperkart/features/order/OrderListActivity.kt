package com.example.paperkart.features.order

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paperkart.R
import com.example.paperkart.databinding.ActivityOrderListBinding

class OrderListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Toolbar/ActionBar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Orders"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Load the OrderFragment into the container
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.orderContainer, OrderFragment())
                .commit()
        }
    }
}