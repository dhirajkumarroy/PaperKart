package com.example.paperkart.features.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.paperkart.MainActivity
import com.example.paperkart.R
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        // ✅ Start Improved Interactive Animation
        val animation = AnimationUtils.loadAnimation(this, R.anim.paperkart_splash_anim)
        binding.ivLogo.startAnimation(animation)

        // Show a loading/progress indicator silently while loading
        // (Handled by the ProgressBar in XML)

        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthAndNavigate()
        }, SPLASH_DELAY)
    }

    private fun checkAuthAndNavigate() {
        val nextActivity = if (session.isLoggedIn()) {
            MainActivity::class.java
        } else {
            AuthActivity::class.java
        }

        startActivity(Intent(this, nextActivity))

        // Dynamic cross-fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        finish() // Ensure back doesn't return to splash
    }

    companion object {
        private const val SPLASH_DELAY = 2200L // Set delay to complete the longer animation smoothly
    }
}