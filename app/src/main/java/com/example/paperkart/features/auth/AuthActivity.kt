package com.example.paperkart.features.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.paperkart.MainActivity
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.AuthApi
import com.example.paperkart.databinding.ActivityAuthBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    private var isEmailMode = true
    private var isOtpSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupWindowInsets()
        setupViewModel()
        setupTabs()
        setupClickListeners()
        setupFieldValidation()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        // Modern approach for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }
    }

    private fun setupViewModel() {
        val api = RetrofitClient.getInstance(this).create(AuthApi::class.java)
        val repo = AuthRepository(api, sessionManager)
        viewModel = AuthViewModel(repo)
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isEmailMode = tab?.position == 0
                isOtpSent = false // Reset OTP state when switching tabs
                updateUiMode()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateUiMode() {
        binding.panelEmail.isVisible = isEmailMode
        binding.panelOtp.isVisible = !isEmailMode

        // Hide OTP input if we haven't sent it yet
        binding.layoutOtpInput.isVisible = !isEmailMode && isOtpSent

        binding.btnLogin.text = when {
            isEmailMode -> "Sign In"
            isOtpSent -> "Verify & Login"
            else -> "Send OTP"
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (isEmailMode) {
                if (validateEmailInputs()) {
                    viewModel.login(
                        email = binding.etEmail.text.toString().trim(),
                        password = binding.etPassword.text.toString()
                    )
                }
            } else {
                val phone = binding.etPhone.text.toString().trim()
                if (isOtpSent) {
                    if (validateOtpInput()) {
                        viewModel.verifyOtp(phone, binding.etOtp.text.toString().trim())
                    }
                } else {
                    if (validatePhoneInput()) {
                        viewModel.sendOtp(phone)
                    }
                }
            }
        }

        binding.btnSendOtp.setOnClickListener {
            if (validatePhoneInput()) {
                viewModel.sendOtp(binding.etPhone.text.toString().trim())
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun setupFieldValidation() {
        binding.etEmail.addTextChangedListener { binding.tilEmail.error = null }
        binding.etPassword.addTextChangedListener { binding.tilPassword.error = null }
        binding.etPhone.addTextChangedListener { binding.tilPhone.error = null }
        binding.etOtp.addTextChangedListener { binding.layoutOtpInput.error = null }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { setLoading(it) }

        viewModel.loginState.observe(this) { user ->
            setLoading(false)

            // 🛡️ ROLE GATE: Ensure only 'USER' can enter
            val role = user.role?.uppercase() ?: "USER"
            if (role == "USER") {
                showSnackbar("Welcome, ${user.name}!")
                navigateToMain()
            } else {
                // Reject Admin/Super Admin
                showAdminErrorDialog(role)
            }
        }

        viewModel.error.observe(this) { message ->
            setLoading(false)
            showSnackbar(message)
        }

        viewModel.otpSent.observe(this) { sent ->
            isOtpSent = sent
            updateUiMode()
            if (sent) showSnackbar("OTP sent successfully")
        }

        viewModel.otpTimer.observe(this) { seconds ->
            if (seconds > 0) {
                binding.btnSendOtp.text = "Resend in ${seconds}s"
                binding.btnSendOtp.isEnabled = false
            } else {
                binding.btnSendOtp.text = "Resend OTP"
                binding.btnSendOtp.isEnabled = true
            }
        }
    }

    private fun showAdminErrorDialog(role: String) {
        // Clear session if any partial data was saved
        sessionManager.clearSession()

        AlertDialog.Builder(this)
            .setTitle("Access Denied")
            .setMessage("Your account is registered as '$role'. Admins must use the PaperKart Web Dashboard. The mobile app is for Customers only.")
            .setPositiveButton("Logout") { d, _ -> d.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun validateEmailInputs(): Boolean {
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty()
        var isValid = true

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email"
            isValid = false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Min. 6 characters required"
            isValid = false
        }
        return isValid
    }

    private fun validatePhoneInput(): Boolean {
        val phone = binding.etPhone.text?.toString().orEmpty().trim()
        if (phone.length < 10) {
            binding.tilPhone.error = "Enter a valid 10-digit number"
            return false
        }
        return true
    }

    private fun validateOtpInput(): Boolean {
        val otp = binding.etOtp.text?.toString().orEmpty().trim()
        if (otp.length != 6) {
            binding.layoutOtpInput.error = "Enter 6-digit OTP"
            return false
        }
        return true
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.isVisible = isLoading
        binding.btnLogin.isEnabled = !isLoading
        if (isLoading) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.accent_primary))
            .setTextColor(getColor(R.color.white))
            .show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}