package com.tiruvear.textiles.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.repositories.AuthRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentRegisterBinding
import com.tiruvear.textiles.ui.main.MainActivity
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val authRepository by lazy { AuthRepositoryImpl() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
    }

    private fun setupViews() {
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                register()
            }
        }
        
        binding.tvLogin.setOnClickListener {
            // Switch to login tab
            (requireActivity() as AuthActivity).goToLoginTab()
        }
    }
    
    private fun validateInputs(): Boolean {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        
        var isValid = true
        
        if (firstName.isEmpty()) {
            binding.tilFirstName.error = "First name is required"
            isValid = false
        } else {
            binding.tilFirstName.error = null
        }
        
        if (lastName.isEmpty()) {
            binding.tilLastName.error = "Last name is required"
            isValid = false
        } else {
            binding.tilLastName.error = null
        }
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        if (phone.isEmpty() || phone.length < 10) {
            binding.tilPhone.error = "Valid phone number is required"
            isValid = false
        } else {
            binding.tilPhone.error = null
        }
        
        if (password.isEmpty() || password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_invalid_password)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        if (confirmPassword != password) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_mismatch)
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        
        return isValid
    }
    
    private fun register() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        
        // Check for empty fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_LONG).show()
            return
        }
        
        (requireActivity() as AuthActivity).showLoading()
        
        lifecycleScope.launch {
            try {
                Log.d("RegisterFragment", "Starting registration for email: $email")
                val result = authRepository.register(email, password, firstName, lastName, phone)
                
                (requireActivity() as AuthActivity).hideLoading()
                
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Registration successful! You can now login.", Toast.LENGTH_LONG).show()
                    // Navigate to login fragment
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment())
                        .commit()
                } else {
                    // Get the specific error message
                    val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                    Log.e("RegisterFragment", "Registration failed: $errorMessage")
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                (requireActivity() as AuthActivity).hideLoading()
                Log.e("RegisterFragment", "Exception during registration: ${e.message}", e)
                Toast.makeText(requireContext(), "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 