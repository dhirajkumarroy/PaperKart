package com.example.paperkart.features.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.AuthApi
import com.example.paperkart.databinding.ActivityResetPasswordBinding
import com.google.android.material.snackbar.Snackbar

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var viewModel: AuthViewModel

    private var resetToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extractToken()
        setupWindowInsets()
        setupViewModel()
        setupClickListeners()
        setupFieldValidation()
        observeViewModel()
    }

    private fun extractToken() {
        // 1. Deep link handling
        val uri = intent?.data
        if (uri != null) {
            resetToken = uri.lastPathSegment ?: ""
        } else {
            // 2. Intent extra handling
            resetToken = intent?.getStringExtra(EXTRA_TOKEN) ?: ""
        }

        if (resetToken.isBlank()) {
            showSnackbar("Invalid or expired reset link.")
            binding.btnResetPassword.isEnabled = false
            // Auto-close if no token after 3 seconds
            binding.root.postDelayed({ navigateToLogin() }, 3000)
        }
    }

    private fun setupWindowInsets() {
        // SDK 36 modern approach
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }
    }

    private fun setupViewModel() {
        val api = RetrofitClient.getInstance(this).create(AuthApi::class.java)
        val repo = AuthRepository(api, SessionManager(this))
        viewModel = AuthViewModel(repo)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.tvBackToLogin.setOnClickListener { navigateToLogin() }

        binding.btnResetPassword.setOnClickListener {
            if (validateAll()) {
                viewModel.resetPassword(
                    token = resetToken,
                    newPassword = binding.etNewPassword.text.toString()
                )
            }
        }
    }

    private fun setupFieldValidation() {
        binding.etNewPassword.addTextChangedListener {
            binding.tilNewPassword.error = null
            updatePasswordStrength(it?.toString() ?: "")
        }
        binding.etConfirmPassword.addTextChangedListener {
            binding.tilConfirmPassword.error = null
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { setLoading(it) }

        viewModel.resetSuccess.observe(this) { msg ->
            setLoading(false)
            showSnackbar(msg)
            // Redirect to Login so they can authenticate and get a fresh token
            binding.root.postDelayed({ navigateToLogin() }, 1500)
        }

        viewModel.error.observe(this) { message ->
            setLoading(false)
            when {
                message.contains("token", true) || message.contains("expired", true) -> {
                    showSnackbar("This link has expired. Please request a new one.")
                    binding.btnResetPassword.isEnabled = false
                }
                message.contains("password", true) -> binding.tilNewPassword.error = message
                else -> showSnackbar(message)
            }
        }
    }

    private fun updatePasswordStrength(password: String) {
        if (password.isEmpty()) {
            binding.passwordStrengthBar.isVisible = false
            binding.tvPasswordStrength.isVisible = false
            return
        }
        binding.passwordStrengthBar.isVisible = true
        binding.tvPasswordStrength.isVisible = true

        val score = calculateStrength(password)
        binding.passwordStrengthBar.progress = score * 25

        // Defined colors directly for SDK 36 compatibility
        val (label, color) = when (score) {
            0, 1 -> "Weak" to ContextCompat.getColor(this, R.color.input_stroke_error)
            2 -> "Fair" to ContextCompat.getColor(this, R.color.accent_secondary)
            3 -> "Strong" to 0xFF10B981.toInt() // Green
            else -> "Excellent" to 0xFF10B981.toInt()
        }

        binding.tvPasswordStrength.text = label
        binding.tvPasswordStrength.setTextColor(color)
        binding.passwordStrengthBar.setIndicatorColor(color)
    }

    private fun calculateStrength(p: String): Int {
        var score = 0
        if (p.length >= 8) score++
        if (p.any { it.isUpperCase() }) score++
        if (p.any { it.isDigit() }) score++
        if (p.any { !it.isLetterOrDigit() }) score++
        return score
    }

    private fun validateAll() = validateNewPassword() && validateConfirmPassword()

    private fun validateNewPassword(): Boolean {
        val v = binding.etNewPassword.text?.toString().orEmpty()
        return if (v.length < 8) {
            binding.tilNewPassword.error = "Minimum 8 characters required"
            false
        } else true
    }

    private fun validateConfirmPassword(): Boolean {
        val pass = binding.etNewPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirmPassword.text?.toString().orEmpty()
        return if (confirm != pass) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            false
        } else true
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.isVisible = isLoading
        binding.btnResetPassword.isEnabled = !isLoading
        if (isLoading) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.accent_primary))
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_TOKEN = "extra_reset_token"
    }
}