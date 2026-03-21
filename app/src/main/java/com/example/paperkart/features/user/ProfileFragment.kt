package com.example.paperkart.features.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.paperkart.R
import com.example.paperkart.core.network.RetrofitClient
import com.example.paperkart.core.utils.SessionManager
import com.example.paperkart.data.api.UserApi
import com.example.paperkart.data.dto.user.UserDto
import com.example.paperkart.databinding.FragmentProfileBinding
import com.example.paperkart.features.auth.AuthActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        session = SessionManager(requireContext())

        // 1. Immediate UI update from local cache
        displayCachedData()

        // 2. Background sync with Node.js backend
        fetchFreshProfile()

        // 3. Set up modern dashboard interactions
        setupClickListeners()
    }

    private fun displayCachedData() {
        val name = session.getUserName() ?: "PaperKart User"
        val email = session.getUserEmail() ?: "Welcome"

        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email
        binding.tvProfileInitials.text = name.take(1).uppercase()
    }

    private fun setupClickListeners() {
        // Edit Profile - You could launch a Dialog or a new Activity here
        binding.btnEditProfile.setOnClickListener {
            showEditNameDialog()
        }

        // Dashboard Row: Saved Addresses
        binding.btnSavedAddress.setOnClickListener {
            // TODO: Replace with fragment navigation or Intent
            Toast.makeText(context, "Opening Saved Addresses...", Toast.LENGTH_SHORT).show()
        }

        // Dashboard Row: My Orders
        binding.btnMyOrders.setOnClickListener {
            // TODO: Navigate to Orders list
            Toast.makeText(context, "Fetching your orders...", Toast.LENGTH_SHORT).show()
        }

        // Logout - With backstack clearing
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun fetchFreshProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitClient.getInstance(requireContext()).create(UserApi::class.java)
                val response = api.getProfile()

                if (response.isSuccessful) {
                    val user = response.body()?.data?.user
                    withContext(Dispatchers.Main) {
                        user?.let {
                            // Sync fresh data to session and UI
                            session.saveUserData(it.name, it.email)
                            updateUi(it)
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently fail, user relies on cached data
            }
        }
    }

    private fun updateUi(user: UserDto) {
        binding.tvProfileName.text = user.name
        binding.tvProfileEmail.text = user.email
        binding.tvProfileInitials.text = user.name?.take(1)?.uppercase()
    }

    /**
     * Logic for updating profile name to match your MERN backend controller.
     * We trigger this when a user edits their name.
     */
    private fun updateProfileName(newName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val api = RetrofitClient.getInstance(requireContext()).create(UserApi::class.java)
                // Sending just the name as per backend: req.body.name
                val response = api.updateProfile(UserDto(
                    name = newName,
                    id = null, email = null, phone = null, role = null,
                    status = null, providers = null, emailVerified = null,
                    createdAt = null, profileImage = null, savedAddress = null,
                    profileCompleted = null
                ))

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        session.saveUserData(newName, session.getUserEmail())
                        binding.tvProfileName.text = newName
                        binding.tvProfileInitials.text = newName.take(1).uppercase()
                        Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Connection Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEditNameDialog() {
        // For your project, you can implement a MaterialAlertDialog
        // with an EditText here to fulfill the "U" in CRUD (Update).
        Toast.makeText(context, "Edit dialog coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun logoutUser() {
        session.clearSession()
        val intent = Intent(requireContext(), AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}