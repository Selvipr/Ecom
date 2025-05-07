package com.tiruvear.textiles.ui.main.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.tiruvear.textiles.data.util.SessionManager

/**
 * Base fragment class for all fragments in the app
 * Provides common functionality and error handling
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    
    protected abstract val TAG: String
    
    /**
     * Creates the view binding for this fragment
     */
    abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    /**
     * Called when the view is created and binding is ready
     */
    abstract fun onBindingReady(savedInstanceState: Bundle?)
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            _binding = createViewBinding(inflater, container)
            return binding.root
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreateView: ${e.message}", e)
            return super.onCreateView(inflater, container, savedInstanceState)
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            onBindingReady(savedInstanceState)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(requireContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Gets the current user's ID from SessionManager
     * @return User ID string or a default guest ID if no user is logged in
     */
    protected fun getCurrentUserId(): String {
        val sessionManager = SessionManager(requireContext())
        return sessionManager.getUserId() ?: "guest_user"
    }
} 