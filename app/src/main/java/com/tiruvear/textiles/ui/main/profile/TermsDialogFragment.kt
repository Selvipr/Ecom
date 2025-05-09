package com.tiruvear.textiles.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.tiruvear.textiles.R
import com.tiruvear.textiles.databinding.DialogInfoBinding

class TermsDialogFragment : DialogFragment() {
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
            title = getString(R.string.terms_and_conditions)
        }
        
        // Set content
        binding.tvContent.text = """
            Terms and Conditions
            Last updated: July 2023
            
            Please read these Terms and Conditions ("Terms") carefully before using the Tiruvear Textiles mobile application (the "App") operated by Tiruvear Textiles ("we", "our", or "us").
            
            By accessing or using the App, you agree to be bound by these Terms. If you disagree with any part of the Terms, you may not access the App.
            
            1. Account Registration
            
            To access certain features of the App, you may be required to register for an account. You agree to provide accurate, current, and complete information during the registration process and to update such information to keep it accurate, current, and complete.
            
            You are responsible for safeguarding the password that you use to access the App and for any activities or actions under your password. We encourage you to use a "strong" password (a password that uses a combination of uppercase and lowercase letters, numbers, and symbols) with your account.
            
            2. Products and Purchases
            
            All product descriptions, prices, and availability are subject to change without notice. We reserve the right to limit the quantities of any products or services that we offer.
            
            We make every effort to display as accurately as possible the colors and images of our products. We cannot guarantee that your device's display of any color will be accurate.
            
            3. Payment and Shipping
            
            Payment for orders must be made at the time of purchase. We accept various payment methods as indicated on the App.
            
            Shipping times and costs will be calculated at checkout. Delivery times are estimates and not guaranteed. We are not responsible for delays due to customs, postal/courier services, or other circumstances beyond our control.
            
            4. Returns and Refunds
            
            Please review our Return Policy for information about returns, refunds, and exchanges.
            
            5. Intellectual Property
            
            The App and its original content, features, and functionality are and will remain the exclusive property of Tiruvear Textiles and its licensors. The App is protected by copyright, trademark, and other laws.
            
            Our trademarks and trade dress may not be used in connection with any product or service without our prior written consent.
            
            6. User Content
            
            By posting, uploading, or submitting any content to the App (including but not limited to product reviews and feedback), you grant us a non-exclusive, royalty-free, perpetual, and worldwide license to use, modify, publicly display, reproduce, and distribute such content on and through the App.
            
            7. Termination
            
            We may terminate or suspend your account and access to the App immediately, without prior notice or liability, for any reason, including without limitation if you breach the Terms.
            
            8. Limitation of Liability
            
            In no event shall Tiruvear Textiles, its directors, employees, partners, agents, suppliers, or affiliates, be liable for any indirect, incidental, special, consequential or punitive damages, including without limitation, loss of profits, data, use, goodwill, or other intangible losses, resulting from your access to or use of or inability to access or use the App.
            
            9. Changes to Terms
            
            We reserve the right to modify or replace these Terms at any time. We will provide notice of any changes by posting the new Terms on the App and updating the "Last updated" date.
            
            10. Contact Us
            
            If you have any questions about these Terms, please contact us at terms@tiruvear.com.
        """.trimIndent()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 