package com.tiruvear.textiles.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.tiruvear.textiles.R
import com.tiruvear.textiles.databinding.DialogInfoBinding

class AboutUsDialogFragment : DialogFragment() {
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
            title = getString(R.string.about_us)
        }
        
        // Set content
        binding.tvContent.text = """
            Tiruvear Textiles is a premium textile brand established in 2005, specializing in traditional and contemporary Indian textiles. Our mission is to preserve and promote the rich textile heritage of India while providing high-quality, sustainable products to our customers.
            
            With over 15 years of experience in the textile industry, we source our materials directly from artisans across India, ensuring fair trade practices and supporting local communities. Our products are made using traditional techniques combined with modern design sensibilities.
            
            Our collection includes a wide range of sarees, fabrics, and garments that showcase the diverse textile traditions of India. We take pride in offering products that reflect the rich cultural heritage of our country while meeting the contemporary needs of our customers.
            
            At Tiruvear Textiles, we are committed to sustainability and ethical business practices. We use eco-friendly materials and processes wherever possible and ensure fair wages for all our artisans and workers.
            
            We value your trust and strive to provide exceptional quality and service. Thank you for choosing Tiruvear Textiles.
        """.trimIndent()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 