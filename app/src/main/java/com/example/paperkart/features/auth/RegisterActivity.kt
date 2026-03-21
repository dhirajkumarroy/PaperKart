package com.example.paperkart.features.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.AuthApi
import com.example.paperkart.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupViewModel()
        setupClickListeners()
        setupFieldValidation()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        // Modern SDK 36/Android 16 style handling
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
        binding.btnRegister.setOnClickListener {
            if (validateAll()) {
                // By default, registration from the app should create a "USER" role
                viewModel.register(
                    name = binding.etName.text.toString().trim(),
                    email = binding.etEmail.text.toString().trim(),
                    password = binding.etPassword.text.toString()
                )
            }
        }
        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun setupFieldValidation() {
        binding.etName.addTextChangedListener { binding.tilName.error = null }
        binding.etEmail.addTextChangedListener { binding.tilEmail.error = null }
        binding.etPassword.addTextChangedListener { binding.tilPassword.error = null }
        binding.etConfirmPassword.addTextChangedListener { binding.tilConfirmPassword.error = null }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading -> setLoading(loading) }

        viewModel.registerSuccess.observe(this) { msg ->
            setLoading(false)
            showSnackbar(msg)
            // Post delayed to allow snackbar to be seen
            binding.root.postDelayed({
                // Go to Login screen instead of Main, because new users often
                // need to verify email first or provide their initial password again
                finish()
            }, 1500)
        }

        viewModel.error.observe(this) { message ->
            setLoading(false)
            // Smart error mapping to specific input fields
            when {
                message.contains("email", ignoreCase = true) -> {
                    binding.tilEmail.error = message
                    binding.etEmail.requestFocus()
                }
                message.contains("name", ignoreCase = true) -> {
                    binding.tilName.error = message
                }
                else -> showSnackbar(message)
            }
        }
    }

    // ── Validation ────────────────────────────────────────────

    private fun validateAll() = validateName() && validateEmail() &&
            validatePassword() && validateConfirmPassword()

    private fun validateName(): Boolean {
        val v = binding.etName.text?.toString().orEmpty().trim()
        return if (v.length < 2) {
            binding.tilName.error = "Please enter your full name"
            false
        } else true
    }

    private fun validateEmail(): Boolean {
        val v = binding.etEmail.text?.toString().orEmpty().trim()
        return if (v.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(v).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            false
        } else true
    }

    private fun validatePassword(): Boolean {
        val v = binding.etPassword.text?.toString().orEmpty()
        return if (v.length < 8) {
            binding.tilPassword.error = "Password must be at least 8 characters"
            false
        } else true
    }

    private fun validateConfirmPassword(): Boolean {
        val pass = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirmPassword.text?.toString().orEmpty()
        return if (confirm != pass) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            false
        } else true
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.isVisible = isLoading
        binding.btnRegister.isEnabled = !isLoading
        if (isLoading) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.accent_primary))
            .setTextColor(getColor(R.color.white))
            .show()
    }
}