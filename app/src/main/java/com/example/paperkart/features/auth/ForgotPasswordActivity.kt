package com.example.paperkart.features.auth

import android.os.Bundle
import android.util.Patterns
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
import com.example.paperkart.databinding.ActivityForgotPasswordBinding
import com.google.android.material.snackbar.Snackbar

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupViewModel()
        setupClickListeners()
        setupFieldValidation()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        // Updated for SDK 36 modern edge-to-edge support
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
        binding.tvBackToLogin.setOnClickListener { finish() }

        binding.btnSendReset.setOnClickListener {
            if (validateEmail()) {
                val email = binding.etEmail.text.toString().trim()
                viewModel.forgotPassword(email)
            }
        }
    }

    private fun setupFieldValidation() {
        binding.etEmail.addTextChangedListener {
            binding.tilEmail.error = null
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading -> setLoading(loading) }

        viewModel.forgotSuccess.observe(this) { msg ->
            setLoading(false)
            // 🎯 SUCCESS UI: Lock inputs and show the success layout
            binding.layoutSuccess.isVisible = true
            binding.tvSuccessMsg.text = msg

            binding.btnSendReset.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.tilEmail.alpha = 0.5f // Visual cue that it's locked

            showSnackbar("Reset link sent! Please check your inbox.")
        }

        viewModel.error.observe(this) { message ->
            setLoading(false)
            if (message.contains("email", ignoreCase = true)) {
                binding.tilEmail.error = message
            } else {
                showSnackbar(message)
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        return if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            false
        } else {
            binding.tilEmail.error = null
            true
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.isVisible = isLoading
        binding.btnSendReset.isEnabled = !isLoading
        if (isLoading) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.accent_primary))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }
}