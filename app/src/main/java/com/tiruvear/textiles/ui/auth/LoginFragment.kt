package com.tiruvear.textiles.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.repositories.AuthRepositoryImpl
import com.tiruvear.textiles.databinding.FragmentLoginBinding
import com.tiruvear.textiles.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val authRepository by lazy { AuthRepositoryImpl() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                login()
            }
        }
        
        binding.btnGuest.setOnClickListener {
            // Skip login and go to main screen as guest
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        
        binding.tvForgotPassword.setOnClickListener {
            // Show forgot password dialog
            showForgotPasswordDialog()
        }
        
        binding.tvRegister.setOnClickListener {
            // Switch to register tab
            (requireActivity() as AuthActivity).goToRegisterTab()
        }
    }
    
    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            return false
        } else {
            binding.tilEmail.error = null
        }
        
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_invalid_password)
            return false
        } else {
            binding.tilPassword.error = null
        }
        
        return true
    }
    
    private fun login() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        
        (requireActivity() as AuthActivity).showLoading()
        
        lifecycleScope.launch {
            try {
                val result = authRepository.login(email, password)
                
                (requireActivity() as AuthActivity).hideLoading()
                
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), R.string.success_login, Toast.LENGTH_SHORT).show()
                    
                    // Navigate to main activity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), R.string.error_login, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                (requireActivity() as AuthActivity).hideLoading()
                Toast.makeText(requireContext(), R.string.error_login, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showForgotPasswordDialog() {
        val email = binding.etEmail.text.toString().trim()
        
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            Toast.makeText(requireContext(), "Please enter your email first", Toast.LENGTH_SHORT).show()
            return
        }
        
        (requireActivity() as AuthActivity).showLoading()
        
        lifecycleScope.launch {
            try {
                val result = authRepository.forgotPassword(email)
                
                (requireActivity() as AuthActivity).hideLoading()
                
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to send reset email", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                (requireActivity() as AuthActivity).hideLoading()
                Toast.makeText(requireContext(), "Failed to send reset email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 