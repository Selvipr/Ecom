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
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tiruvear.textiles.R
import com.tiruvear.textiles.data.models.User
import com.tiruvear.textiles.data.repositories.AuthRepositoryImpl
import com.tiruvear.textiles.data.util.SessionManager
import com.tiruvear.textiles.databinding.FragmentProfileBinding
import com.tiruvear.textiles.ui.auth.AuthActivity
import com.tiruvear.textiles.ui.main.base.BaseFragment
import kotlinx.coroutines.launch
import java.io.File

/**
 * ProfileFragment displays the user's profile information and account settings
 */
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    override val TAG: String = "ProfileFragment"
    
    private val authRepository by lazy { AuthRepositoryImpl(requireContext()) }
    private lateinit var sessionManager: SessionManager
    private var selectedImageUri: Uri? = null
    
    // Activity result launcher for image picking
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                updateProfileImage(uri)
            }
        }
    }
    
    // Activity result launcher for profile edit
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Reload user data to reflect any changes
            loadUserData()
        }
    }
    
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }
    
    override fun onBindingReady(savedInstanceState: Bundle?) {
        sessionManager = SessionManager(requireContext())
        setupProfileUpdateListener()
        setupViews()
        loadUserData()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload user data whenever the fragment becomes visible
        loadUserData()
    }
    
    private fun setupProfileUpdateListener() {
        // Set up listener for profile updates from dialog
        childFragmentManager.setFragmentResultListener("profile_updated", this) { _, bundle ->
            val success = bundle.getBoolean("success", false)
            val userId = bundle.getString("userId")
            
            if (success && userId != null) {
                // Reload user data when profile is updated
                loadUserData()
            }
        }
    }
    
    private fun setupViews() {
        // Set up profile image click to change image
        binding.ivProfileImage.setOnClickListener {
            // Only allow image change if logged in
            if (sessionManager.getUserId() != null) {
                launchImagePicker()
            } else {
                showToast("Please login to change profile picture")
            }
        }
        
        // Debug: Long press on profile image to test profile data
        binding.ivProfileImage.setOnLongClickListener {
            val intent = Intent(requireContext(), ProfileTestActivity::class.java)
            startActivity(intent)
            true
        }
        
        // Set up click listeners for the various profile options
        binding.btnEditProfile.setOnClickListener {
            if (sessionManager.getUserId() != null) {
                navigateToEditProfile()
            } else {
                navigateToLogin()
            }
        }
        
        binding.tvMyOrders.setOnClickListener {
            try {
                findNavController().navigate(R.id.navigation_orders)
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to orders: ${e.message}", e)
                showToast("Could not navigate to orders")
            }
        }
        
        binding.tvMyAddresses.setOnClickListener {
            navigateToAddresses()
        }
        
        binding.tvNotifications.setOnClickListener {
            navigateToNotifications()
        }
        
        binding.tvAboutUs.setOnClickListener {
            navigateToAboutUs()
        }
        
        binding.tvPrivacyPolicy.setOnClickListener {
            navigateToPrivacyPolicy()
        }
        
        binding.tvTerms.setOnClickListener {
            navigateToTerms()
        }
        
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }
    
    private fun loadUserData() {
        val userId = sessionManager.getUserId()
        
        // If not logged in, show guest UI
        if (userId == null) {
            displayGuestUI()
            return
        }
        
        // Show loading UI
        showLoading(true)
        
        // Load user data
        lifecycleScope.launch {
            try {
                val userResult = authRepository.getUserProfile(userId)
                
                showLoading(false)
                
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    if (user != null) {
                        displayUserData(user)
                    } else {
                        displayGuestUI()
                        showToast("Could not load user data")
                    }
                } else {
                    displayGuestUI()
                    Log.e(TAG, "Error loading user: ${userResult.exceptionOrNull()?.message}")
                    showToast("Error loading profile")
                }
            } catch (e: Exception) {
                showLoading(false)
                displayGuestUI()
                Log.e(TAG, "Exception loading user profile: ${e.message}", e)
                showToast("Error loading profile")
            }
        }
    }
    
    private fun displayUserData(user: User) {
        binding.tvUserName.text = user.name
        binding.tvUserEmail.text = user.email
        
        // Display phone number if available
        if (!user.phone.isNullOrEmpty()) {
            binding.tvUserPhone.text = user.phone
            binding.tvUserPhone.visibility = View.VISIBLE
        } else {
            binding.tvUserPhone.visibility = View.GONE
        }
        
        // Set up profile image using Glide
        loadProfileImage(user.profileImageUrl)
        
        // Show logged-in UI elements
        binding.btnLogout.visibility = View.VISIBLE
        binding.btnEditProfile.text = getString(R.string.edit_profile)
    }
    
    private fun loadProfileImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
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
    
    private fun updateProfileImage(uri: Uri) {
        // Show loading while uploading
        showLoading(true)
        
        // Update the UI immediately for better user experience
        Glide.with(requireContext())
            .load(uri)
            .circleCrop()
            .into(binding.ivProfileImage)
        
        // In a real app, upload the image to server
        lifecycleScope.launch {
            try {
                // Get file from URI to upload to Supabase
                val file = getFileFromUri(uri) ?: throw Exception("Failed to create file from URI")
                
                // Upload to Supabase Storage
                val userId = sessionManager.getUserId() ?: throw Exception("User not logged in")
                val fileExtension = getFileExtension(uri)
                val fileName = "profile_${userId}_${System.currentTimeMillis()}.$fileExtension"
                
                val uploadResult = authRepository.uploadProfileImage(file, fileName)
                
                if (uploadResult.isSuccess) {
                    // Get the URL of the uploaded image
                    val imageUrl = uploadResult.getOrNull()
                    
                    if (imageUrl != null) {
                        // Get current user data
                        val userResult = authRepository.getUserProfile(userId)
                        
                        if (userResult.isSuccess) {
                            val user = userResult.getOrNull()
                            
                            if (user != null) {
                                // Update user profile with new image URL
                                val updatedUser = user.copy(profileImageUrl = imageUrl)
                                val updateResult = authRepository.updateUserProfile(updatedUser)
                                
                                if (updateResult.isSuccess) {
                                    showLoading(false)
                                    showToast("Profile picture updated successfully")
                                    loadUserData() // Reload user data to reflect changes
                                } else {
                                    showLoading(false)
                                    showToast("Failed to update profile with new image")
                                }
                            } else {
                                showLoading(false)
                                showToast("Failed to get user data")
                            }
                        } else {
                            showLoading(false)
                            showToast("Failed to get user data")
                        }
                    } else {
                        showLoading(false)
                        showToast("Failed to get uploaded image URL")
                    }
                } else {
                    showLoading(false)
                    Log.e(TAG, "Error uploading profile image: ${uploadResult.exceptionOrNull()?.message}")
                    showToast("Failed to upload profile picture")
                }
            } catch (e: Exception) {
                showLoading(false)
                Log.e(TAG, "Error updating profile image: ${e.message}", e)
                showToast("Failed to update profile picture")
            }
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
    
    private fun displayGuestUI() {
        binding.tvUserName.text = getString(R.string.guest_user)
        binding.tvUserEmail.text = getString(R.string.login_to_access)
        binding.btnEditProfile.text = getString(R.string.login)
        binding.tvUserPhone.visibility = View.GONE
        
        // Change edit profile button to login button
        binding.btnEditProfile.setOnClickListener {
            navigateToLogin()
        }
        
        // Set placeholder image
        Glide.with(requireContext())
            .load(R.drawable.ic_profile_placeholder)
            .circleCrop()
            .into(binding.ivProfileImage)
        
        // Hide logout button for guest
        binding.btnLogout.visibility = View.GONE
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
        
        binding.btnEditProfile.isEnabled = !isLoading
        binding.btnLogout.isEnabled = !isLoading
    }
    
    private fun showLogoutConfirmation() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ -> logout() }
            .setNegativeButton("Cancel", null)
        
        builder.create().show()
    }
    
    private fun logout() {
        try {
            // Clear user session
            sessionManager.clearSession()
            
            // Navigate to login
            navigateToLogin()
            
            showToast("Logged out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}", e)
            showToast("Error logging out")
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToEditProfile() {
        // Navigate to edit profile dialog
        val editProfileDialog = EditProfileDialogFragment.newInstance(
            sessionManager.getUserId() ?: return
        )
        
        editProfileDialog.show(childFragmentManager, "edit_profile")
    }
    
    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
    
    private fun navigateToAddresses() {
        val addresses = getAddresses()
        showAddressListDialog(addresses)
    }
    
    private fun navigateToNotifications() {
        // In a real app, you would navigate to the notifications screen
        // For now, just show a toast
        showToast("Notifications feature coming soon")
    }
    
    private fun navigateToAboutUs() {
        // In a real app, you would navigate to the about us screen
        // For now, just show a toast
        val aboutDialog = AboutUsDialogFragment()
        aboutDialog.show(childFragmentManager, "about_us")
    }
    
    private fun navigateToPrivacyPolicy() {
        // In a real app, you would navigate to the privacy policy screen
        // For now, just show a toast
        val privacyDialog = PrivacyPolicyDialogFragment()
        privacyDialog.show(childFragmentManager, "privacy_policy")
    }
    
    private fun navigateToTerms() {
        // In a real app, you would navigate to the terms and conditions screen
        // For now, just show a toast
        val termsDialog = TermsDialogFragment()
        termsDialog.show(childFragmentManager, "terms")
    }
    
    // ViewHolder for addresses
    inner class AddressViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val tvAddressLine1: TextView = itemView.findViewById(R.id.tv_address_line1)
        val tvAddressLine2: TextView = itemView.findViewById(R.id.tv_address_line2)
        val tvCityStatePin: TextView = itemView.findViewById(R.id.tv_city_state_pin)
        val tvDefault: TextView = itemView.findViewById(R.id.tv_default)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)
    }
    
    private fun showAddressListDialog(addresses: List<com.tiruvear.textiles.data.models.Address>) {
        if (addresses.isEmpty()) {
            showAddAddressDialog()
            return
        }
        
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_address_list, null)
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("My Addresses")
            .setView(dialogView)
            .setPositiveButton("Add New Address") { _, _ -> showAddAddressDialog() }
            .setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
        
        val dialog = builder.create()
        dialog.show()
        
        // Set up RecyclerView to display addresses
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_addresses)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        
        // Create an adapter for the addresses
        val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<AddressViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
                return AddressViewHolder(itemView)
            }
            
            override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
                val address = addresses[position]
                
                holder.tvAddressLine1.text = address.addressLine1
                holder.tvAddressLine2.text = address.addressLine2 ?: ""
                holder.tvCityStatePin.text = "${address.city}, ${address.state} - ${address.pincode}"
                
                if (address.isDefault) {
                    holder.tvDefault.visibility = View.VISIBLE
                } else {
                    holder.tvDefault.visibility = View.GONE
                }
                
                // Set up edit and delete buttons
                holder.btnEdit.setOnClickListener {
                    dialog.dismiss()
                    showEditAddressDialog(address)
                }
                
                holder.btnDelete.setOnClickListener {
                    dialog.dismiss()
                    showDeleteAddressConfirmation(address)
                }
            }
            
            override fun getItemCount(): Int = addresses.size
        }
        
        recyclerView.adapter = adapter
    }
    
    private fun showEditAddressDialog(address: com.tiruvear.textiles.data.models.Address) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_address, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Address")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        
        val dialog = builder.create()
        dialog.show()
        
        // Get references to the form fields and fill them with the address data
        val etAddressLine1 = dialogView.findViewById<android.widget.EditText>(R.id.et_address_line1)
        val etAddressLine2 = dialogView.findViewById<android.widget.EditText>(R.id.et_address_line2)
        val etCity = dialogView.findViewById<android.widget.EditText>(R.id.et_city)
        val etState = dialogView.findViewById<android.widget.EditText>(R.id.et_state)
        val etPincode = dialogView.findViewById<android.widget.EditText>(R.id.et_pincode)
        val cbDefault = dialogView.findViewById<android.widget.CheckBox>(R.id.cb_default_address)
        
        // Fill the form fields with the address data
        etAddressLine1.setText(address.addressLine1)
        etAddressLine2.setText(address.addressLine2 ?: "")
        etCity.setText(address.city)
        etState.setText(address.state)
        etPincode.setText(address.pincode)
        cbDefault.isChecked = address.isDefault
        
        // Set a click listener for the positive button to validate form
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // Validate inputs
            val addressLine1 = etAddressLine1.text.toString().trim()
            val city = etCity.text.toString().trim()
            val state = etState.text.toString().trim()
            val pincode = etPincode.text.toString().trim()
            
            var isValid = true
            if (addressLine1.isEmpty()) {
                etAddressLine1.error = "Address is required"
                isValid = false
            }
            
            if (city.isEmpty()) {
                etCity.error = "City is required"
                isValid = false
            }
            
            if (state.isEmpty()) {
                etState.error = "State is required"
                isValid = false
            }
            
            if (pincode.isEmpty() || pincode.length != 6) {
                etPincode.error = "Valid 6-digit pincode is required"
                isValid = false
            }
            
            if (isValid) {
                // Get the addressLine2 (which is optional)
                val addressLine2 = etAddressLine2.text.toString().trim().let { 
                    if (it.isEmpty()) null else it 
                }
                val isDefault = cbDefault.isChecked
                
                // Create updated address
                val updatedAddress = com.tiruvear.textiles.data.models.Address(
                    id = address.id,
                    userId = address.userId,
                    addressLine1 = addressLine1,
                    addressLine2 = addressLine2,
                    city = city,
                    state = state,
                    pincode = pincode,
                    isDefault = isDefault,
                    createdAt = address.createdAt,
                    updatedAt = java.util.Date()
                )
                
                // Update address in the list
                updateAddress(updatedAddress)
                
                // Dismiss the dialog after successful validation and save
                dialog.dismiss()
                
                // Show confirmation to user
                showToast("Address updated successfully")
                
                // Refresh the address list
                showAddressListDialog(getAddresses())
            }
        }
    }
    
    private fun updateAddress(address: com.tiruvear.textiles.data.models.Address) {
        try {
            val prefs = requireContext().getSharedPreferences("addresses", android.content.Context.MODE_PRIVATE)
            val addressesString = prefs.getString("addressList", null) ?: return
            
            val type = object : com.google.gson.reflect.TypeToken<ArrayList<com.tiruvear.textiles.data.models.Address>>() {}.type
            val addressesList = com.google.gson.Gson().fromJson<ArrayList<com.tiruvear.textiles.data.models.Address>>(addressesString, type) ?: return
            
            // Find and replace the address with matching ID
            val updatedList = addressesList.map { 
                if (it.id == address.id) address else it 
            }
            
            // If the updated address is the default, make all others non-default
            val finalList = if (address.isDefault) {
                updatedList.map {
                    if (it.id != address.id) it.copy(isDefault = false) else it
                }
            } else {
                updatedList
            }
            
            // Save updated list
            val editor = prefs.edit()
            val json = com.google.gson.Gson().toJson(finalList)
            editor.putString("addressList", json)
            editor.apply()
            
            Log.d(TAG, "Address updated successfully, new list: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating address: ${e.message}", e)
        }
    }
    
    private fun showDeleteAddressConfirmation(address: com.tiruvear.textiles.data.models.Address) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Address")
            .setMessage("Are you sure you want to delete this address?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete the address
                deleteAddress(address.id)
                showToast("Address deleted successfully")
                
                // Refresh address list
                val updatedAddresses = getAddresses()
                if (updatedAddresses.isNotEmpty()) {
                    showAddressListDialog(updatedAddresses)
                } else {
                    showAddAddressDialog()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Show the address list again
                showAddressListDialog(getAddresses())
            }
        
        builder.create().show()
    }
    
    private fun deleteAddress(addressId: String) {
        try {
            val prefs = requireContext().getSharedPreferences("addresses", android.content.Context.MODE_PRIVATE)
            val addressesString = prefs.getString("addressList", null) ?: return
            
            val type = object : com.google.gson.reflect.TypeToken<ArrayList<com.tiruvear.textiles.data.models.Address>>() {}.type
            val addressesList = com.google.gson.Gson().fromJson<ArrayList<com.tiruvear.textiles.data.models.Address>>(addressesString, type) ?: return
            
            // Remove address with matching ID
            val updatedList = addressesList.filter { it.id != addressId }
            
            // If we deleted the default address and there are others left, make the first one default
            val finalList = if (addressesList.any { it.id == addressId && it.isDefault } && updatedList.isNotEmpty()) {
                updatedList.mapIndexed { index, address ->
                    if (index == 0) address.copy(isDefault = true) else address
                }
            } else {
                updatedList
            }
            
            // Save updated list
            val editor = prefs.edit()
            val json = com.google.gson.Gson().toJson(finalList)
            editor.putString("addressList", json)
            editor.apply()
            
            Log.d(TAG, "Address deleted successfully, new list: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting address: ${e.message}", e)
        }
    }
    
    private fun showAddAddressDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_address, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add New Address")
            .setView(dialogView)
            .setPositiveButton("Save", null) // Set null initially to prevent automatic dismissal
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        
        val dialog = builder.create()
        dialog.show()
        
        // Get references to the form fields
        val etAddressLine1 = dialogView.findViewById<android.widget.EditText>(R.id.et_address_line1)
        val etAddressLine2 = dialogView.findViewById<android.widget.EditText>(R.id.et_address_line2)
        val etCity = dialogView.findViewById<android.widget.EditText>(R.id.et_city)
        val etState = dialogView.findViewById<android.widget.EditText>(R.id.et_state)
        val etPincode = dialogView.findViewById<android.widget.EditText>(R.id.et_pincode)
        val cbDefault = dialogView.findViewById<android.widget.CheckBox>(R.id.cb_default_address)
        
        // Set a click listener for the positive button to validate form
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // Validate inputs
            val addressLine1 = etAddressLine1.text.toString().trim()
            val city = etCity.text.toString().trim()
            val state = etState.text.toString().trim()
            val pincode = etPincode.text.toString().trim()
            
            var isValid = true
            if (addressLine1.isEmpty()) {
                etAddressLine1.error = "Address is required"
                isValid = false
            }
            
            if (city.isEmpty()) {
                etCity.error = "City is required"
                isValid = false
            }
            
            if (state.isEmpty()) {
                etState.error = "State is required"
                isValid = false
            }
            
            if (pincode.isEmpty() || pincode.length != 6) {
                etPincode.error = "Valid 6-digit pincode is required"
                isValid = false
            }
            
            if (isValid) {
                // Get the addressLine2 (which is optional)
                val addressLine2 = etAddressLine2.text.toString().trim().let { 
                    if (it.isEmpty()) null else it 
                }
                val isDefault = cbDefault.isChecked
                
                // Create new address
                val address = com.tiruvear.textiles.data.models.Address(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = sessionManager.getUserId() ?: "guest",
                    addressLine1 = addressLine1,
                    addressLine2 = addressLine2,
                    city = city,
                    state = state,
                    pincode = pincode,
                    isDefault = isDefault,
                    createdAt = java.util.Date(),
                    updatedAt = java.util.Date()
                )
                
                // Add address to the list
                saveAddress(address)
                
                // Dismiss the dialog after successful validation and save
                dialog.dismiss()
                
                // Show confirmation to user
                showToast("Address added successfully")
                
                // Refresh the address list
                showAddressListDialog(getAddresses())
            }
        }
    }
    
    private fun saveAddress(address: com.tiruvear.textiles.data.models.Address) {
        try {
            val prefs = requireContext().getSharedPreferences("addresses", android.content.Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            // Get existing addresses
            val addressesString = prefs.getString("addressList", null) ?: "[]"
            val type = object : com.google.gson.reflect.TypeToken<ArrayList<com.tiruvear.textiles.data.models.Address>>() {}.type
            val addressesList = com.google.gson.Gson().fromJson<ArrayList<com.tiruvear.textiles.data.models.Address>>(addressesString, type) ?: ArrayList()
            
            // If the new address is the default, make all others non-default
            val updatedList = if (address.isDefault) {
                addressesList.map { it.copy(isDefault = false) }
            } else {
                addressesList
            }
            
            // Add new address
            val finalList = updatedList.toMutableList()
            finalList.add(address)
            
            // Save updated list
            val json = com.google.gson.Gson().toJson(finalList)
            editor.putString("addressList", json)
            editor.apply()
            
            Log.d(TAG, "Address saved successfully: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving address: ${e.message}", e)
        }
    }
    
    private fun getAddresses(): List<com.tiruvear.textiles.data.models.Address> {
        try {
            // Get saved addresses from SharedPreferences
            val prefs = requireContext().getSharedPreferences("addresses", android.content.Context.MODE_PRIVATE)
            val addressesString = prefs.getString("addressList", null)
            
            if (addressesString != null) {
                val type = object : com.google.gson.reflect.TypeToken<ArrayList<com.tiruvear.textiles.data.models.Address>>() {}.type
                val addressesList = com.google.gson.Gson().fromJson<ArrayList<com.tiruvear.textiles.data.models.Address>>(addressesString, type)
                
                if (addressesList != null && addressesList.isNotEmpty()) {
                    // Return addresses that belong to the current user or guest
                    val userId = sessionManager.getUserId() ?: "guest"
                    return addressesList.filter { it.userId == userId }
                }
            }
            
            // Return empty list if no addresses found
            return emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting addresses: ${e.message}", e)
            return emptyList()
        }
    }
} 