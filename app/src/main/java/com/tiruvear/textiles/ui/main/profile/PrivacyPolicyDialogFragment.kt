package com.tiruvear.textiles.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.tiruvear.textiles.R
import com.tiruvear.textiles.databinding.DialogInfoBinding

class PrivacyPolicyDialogFragment : DialogFragment() {
    private var _binding: DialogInfoBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_TiruvearTextiles_Dialog)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogInfoBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupView()
    }
    
    private fun setupView() {
        binding.toolbar.apply {
            setNavigationOnClickListener { dismiss() }
            title = getString(R.string.privacy_policy)
        }
        
        // Set content
        binding.tvContent.text = """
            Privacy Policy
            Last updated: July 2023
            
            Tiruvear Textiles ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and disclose your information when you use our mobile application (the "App").
            
            Information We Collect
            
            Personal Information: When you create an account or place an order, we collect your name, email address, phone number, shipping address, and payment information.
            
            Usage Information: We collect information about how you interact with our App, including the pages you view, the products you browse, and your search queries.
            
            Device Information: We collect information about your device, including device type, operating system, and unique device identifiers.
            
            How We Use Your Information
            
            To provide and maintain our services, including processing orders and customer support
            To personalize your experience and deliver content and product offerings relevant to your interests
            To improve our App and develop new features
            To communicate with you about orders, promotions, and updates
            To detect and prevent fraud and secure our services
            
            Data Sharing and Disclosure
            
            Service Providers: We share information with third-party vendors who provide services on our behalf, such as payment processing and shipping.
            
            Legal Requirements: We may disclose your information if required by law or in response to valid requests by public authorities.
            
            Business Transfers: If we are involved in a merger, acquisition, or sale of assets, your information may be transferred as part of that transaction.
            
            Your Rights and Choices
            
            You can access, update, or delete your personal information through your account settings in the App.
            
            You can opt-out of receiving promotional emails by following the unsubscribe instructions in the emails.
            
            Data Security
            
            We implement appropriate security measures to protect your personal information from unauthorized access, alteration, or disclosure.
            
            Changes to This Privacy Policy
            
            We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the "Last updated" date.
            
            Contact Us
            
            If you have any questions about this Privacy Policy, please contact us at privacy@tiruvear.com.
        """.trimIndent()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}