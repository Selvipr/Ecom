package com.tiruvear.textiles.ui.main.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.User
import com.tiruvear.textiles.data.repositories.AuthRepositoryImpl
import com.tiruvear.textiles.databinding.DialogEditProfileBinding
import kotlinx.coroutines.launch
import java.io.File

class EditProfileDialogFragment : DialogFragment() {
    private var _binding: DialogEditProfileBinding? = null
    private val binding get() = _binding!!
    
    private val authRepository by lazy { AuthRepositoryImpl(requireContext()) }
    private lateinit var userId: String
    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null
    
    // Activity result launcher for image picking
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                updateProfileImagePreview(uri)
            }
        }
    }
    
    companion object {
        private const val TAG = "EditProfileDialog"
        private const val ARG_USER_ID = "user_id"
        
        fun newInstance(userId: String): EditProfileDialogFragment {
            val fragment = EditProfileDialogFragment()
            val args = Bundle().apply {
                putString(ARG_USER_ID, userId)
            }
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_TiruvearTextiles_FullScreenDialog)
        
        userId = arguments?.getString(ARG_USER_ID) ?: throw IllegalArgumentException("User ID is required")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        loadUserData()
        setupListeners()
    }
    
    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener { dismiss() }
            title = "Edit Profile"
            inflateMenu(R.menu.menu_edit_profile)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_save -> {
                        saveUserData()
                        true
                    }
                    else -> false
                }
            }
        }
    }
    
    private fun loadUserData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val userResult = authRepository.getUserProfile(userId)
                
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    if (user != null) {
                        currentUser = user
                        populateUserData(user)
                    } else {
                        showToast("Could not load user data")
                        dismiss()
                    }
                } else {
                    Log.e(TAG, "Error loading user: ${userResult.exceptionOrNull()?.message}")
                    showToast("Error loading profile")
                    dismiss()
                }
                
                showLoading(false)
            } catch (e: Exception) {
                showLoading(false)
                Log.e(TAG, "Exception loading user profile: ${e.message}", e)
                showToast("Error loading profile")
                dismiss()
            }
        }
    }
    
    private fun populateUserData(user: User) {
        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        binding.etPhone.setText(user.phone ?: "")
        
        // Load profile image using Glide
        if (!user.profileImageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(user.profileImageUrl)
                .apply(RequestOptions()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop())
                .into(binding.ivProfileImage)
        } else {
            // Set placeholder image
            Glide.with(requireContext())
                .load(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(binding.ivProfileImage)
        }
    }
    
    private fun setupListeners() {
        // Setup profile image click listener for photo change
        binding.tvChangePhoto.setOnClickListener {
            launchImagePicker()
        }
        
        binding.ivProfileImage.setOnClickListener {
            launchImagePicker()
        }
    }
    
    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
    
    private fun updateProfileImagePreview(uri: Uri) {
        // Update the UI immediately for better user experience
        Glide.with(requireContext())
            .load(uri)
            .circleCrop()
            .into(binding.ivProfileImage)
    }
    
    private fun saveUserData() {
        // Validate inputs
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        
        // Simple validation
        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return
        }
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Valid email is required"
            return
        }
        
        if (phone.isNotEmpty() && !android.util.Patterns.PHONE.matcher(phone).matches()) {
            binding.etPhone.error = "Enter a valid phone number"
            return
        }
        
        showLoading(true)
        
        // Create updated user object
        val updatedUser = currentUser?.copy(
            name = name,
            email = email,
            phone = if (phone.isEmpty()) null else phone
        ) ?: return
        
        lifecycleScope.launch {
            try {
                // First, handle profile image upload if selected
                var profileImageUrl = currentUser?.profileImageUrl
                
                if (selectedImageUri != null) {
                    // Upload image to Supabase storage
                    val imageUploadResult = uploadProfileImage(selectedImageUri!!)
                    if (imageUploadResult.isSuccess) {
                        profileImageUrl = imageUploadResult.getOrNull()
                    } else {
                        Log.e(TAG, "Failed to upload profile image: ${imageUploadResult.exceptionOrNull()?.message}")
                        // Continue with profile update even if image upload fails
                    }
                }
                
                // Update the user with the new image URL if available
                val userToUpdate = updatedUser.copy(
                    profileImageUrl = profileImageUrl
                )
                
                // Update user profile in Supabase
                val updateResult = authRepository.updateUserProfile(userToUpdate)
                
                if (updateResult.isSuccess) {
                    // Update successful
                    showToast("Profile updated successfully")
                    
                    // Set result to notify parent fragment to refresh data
                    parentFragmentManager.setFragmentResult(
                        "profile_updated",
                        Bundle().apply {
                            putBoolean("success", true)
                            putString("userId", userId)
                        }
                    )
                    
                    dismiss()
                } else {
                    // Update failed
                    showLoading(false)
                    showToast("Failed to update profile: ${updateResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showLoading(false)
                Log.e(TAG, "Error updating profile: ${e.message}", e)
                showToast("Failed to update profile")
            }
        }
    }
    
    private suspend fun uploadProfileImage(uri: Uri): Result<String> {
        return try {
            // Get file from URI
            val file = getFileFromUri(uri)
            if (file == null) {
                Log.e(TAG, "Failed to create file from URI")
                return Result.failure(Exception("Failed to create file from URI"))
            }
            
            // Upload to Supabase Storage
            val fileExtension = getFileExtension(uri)
            val fileName = "profile_${userId}_${System.currentTimeMillis()}.$fileExtension"
            
            // Call repository method to upload file
            val uploadResult = authRepository.uploadProfileImage(file, fileName)
            
            if (uploadResult.isSuccess) {
                Result.success(uploadResult.getOrNull() ?: "")
            } else {
                Result.failure(uploadResult.exceptionOrNull() ?: Exception("Unknown error during upload"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile image: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private fun getFileFromUri(uri: Uri): File? {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("profile_", ".tmp", requireContext().cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }
            inputStream.close()
            return tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error creating file from URI: ${e.message}", e)
            return null
        }
    }
    
    private fun getFileExtension(uri: Uri): String {
        val contentResolver = requireContext().contentResolver
        val mimeType = contentResolver.getType(uri) ?: return "jpg"
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.toolbar.menu.findItem(R.id.action_save)?.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.toolbar.menu.findItem(R.id.action_save)?.isEnabled = true
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 