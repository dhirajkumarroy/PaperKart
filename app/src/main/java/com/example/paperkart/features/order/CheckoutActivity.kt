package com.example.paperkart.features.order

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.data.api.OrderApi
import com.example.paperkart.data.dto.order.OrderDto
import com.example.paperkart.data.dto.order.ShippingAddressRequest
import com.example.paperkart.databinding.ActivityCheckoutBinding
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject
import java.util.UUID

class CheckoutActivity : AppCompatActivity(), PaymentResultWithDataListener {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var viewModel: OrderViewModel
    private var currentOrderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()

        // Preload Razorpay to avoid delay when opening the sheet
        Checkout.preload(applicationContext)

        // Initializing the ViewModel with the Retrofit Singleton
        val api = RetrofitClient.getInstance(this).create(OrderApi::class.java)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return OrderViewModel(api) as T
            }
        })[OrderViewModel::class.java]

        setupObservers()

        binding.btnPlaceOrder.setOnClickListener {
            handleCheckout()
        }
    }

    private fun setupWindowInsets() {
        // Use WindowInsets to handle the status bar (time/notifications)
        // so the layout doesn't hide behind it when using light status bar or full screen modes.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Ensure the status bar is visible and light so icons (time, battery) are dark
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    private fun setupObservers() {
        // Observe Order Creation and Payment Verification
        viewModel.orderResult.observe(this) { order ->
            order?.let {
                currentOrderId = it.id

                // Determine next step based on Order status and Payment method
                when {
                    it.payment.method == "ONLINE" && it.status == "PENDING_PAYMENT" -> {
                        startRazorpayPayment(it)
                    }
                    it.status == "PLACED" || it.payment.status == "PAID" -> {
                        navigateToSuccess(it.id)
                        // Important: Nullify to prevent re-navigation on orientation change
                        viewModel.orderResult.value = null
                    }
                }
            }
        }

        // Handle Global Error Toasting
        viewModel.error.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.error.value = null
            }
        }

        // Toggle Loading State on Button
        viewModel.isLoading.observe(this) { loading ->
            binding.btnPlaceOrder.isEnabled = !loading
            binding.btnPlaceOrder.text = if (loading) "Processing..." else "Place Order"
        }

        // Trigger OTP Dialog for COD
        viewModel.otpSent.observe(this) { sent ->
            if (sent == true) {
                showOtpDialog(getAddressFromUI())
                viewModel.otpSent.value = false
            }
        }
    }

    private fun handleCheckout() {
        val address = getAddressFromUI()
        if (address.name.isBlank() || address.phone.length != 10) {
            Toast.makeText(this, "Please provide valid contact details", Toast.LENGTH_SHORT).show()
            return
        }

        val method = if (binding.rbCod.isChecked) "COD" else "ONLINE"
        val idempotencyKey = UUID.randomUUID().toString()

        if (method == "COD") {
            viewModel.sendCodOtp(address.phone)
        } else {
            viewModel.placeOrder(address, "ONLINE", idempotencyKey)
        }
    }

    private fun startRazorpayPayment(order: OrderDto) {
        if (isFinishing) return // Prevent crash if activity is closing

        val checkout = Checkout()
        // Pro-tip: Move this to a secure BuildConfig field later
        checkout.setKeyID("rzp_test_YOUR_ACTUAL_KEY")

        try {
            val options = JSONObject().apply {
                put("name", "PaperKart")
                put("description", "Secure Payment for Order #${order.id}")
                put("order_id", order.payment.razorpayOrderId)
                put("amount", (order.totalAmount * 100).toInt()) // Amount in paise
                put("currency", "INR")
                put("prefill.contact", order.shippingAddress.phone)

                val retryObj = JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 4)
                }
                put("retry", retryObj)
            }
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to launch payment gateway", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        currentOrderId?.let { id ->
            viewModel.confirmOnlinePayment(
                orderId = id,
                paymentId = razorpayPaymentId ?: "",
                signature = paymentData?.signature ?: ""
            )
        }
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "Payment was not successful", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSuccess(orderId: String) {
        val intent = Intent(this, OrderSuccessActivity::class.java).apply {
            putExtra("EXTRA_ORDER_ID", orderId)
            // Clear the task so the user cannot back-button into the payment flow
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showOtpDialog(address: ShippingAddressRequest) {
        if (isFinishing) return

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_otp_verify, null)
        val etOtp = dialogView.findViewById<EditText>(R.id.etOtp)
        val btnVerify = dialogView.findViewById<Button>(R.id.btnVerify)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnVerify.setOnClickListener {
            val otp = etOtp.text.toString().trim()
            if (otp.length == 6) {
                viewModel.verifyAndPlaceOrder(address, otp, UUID.randomUUID().toString())
                dialog.dismiss()
            } else {
                etOtp.error = "6-digit OTP required"
            }
        }
        dialog.show()
    }

    private fun getAddressFromUI() = ShippingAddressRequest(
        name = binding.etName.text.toString().trim(),
        phone = binding.etPhone.text.toString().trim(),
        address = binding.etAddress.text.toString().trim(),
        landmark = binding.etLandmark.text.toString().trim(),
        city = binding.etCity.text.toString().trim(),
        state = binding.etState.text.toString().trim(),
        pincode = binding.etPincode.text.toString().trim()
    )
}
