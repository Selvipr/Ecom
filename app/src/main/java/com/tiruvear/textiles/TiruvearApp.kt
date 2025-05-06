package com.tiruvear.textiles

import android.app.Application
import android.util.Log
import android.widget.Toast
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class TiruvearApp : Application() {

    companion object {
        private const val TAG = "TiruvearApp"
        
        lateinit var supabaseClient: SupabaseClient
            private set
            
        const val SUPABASE_URL = "https://spgxymonqkttpzfsztvp.supabase.co"
        const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNwZ3h5bW9ucWt0dHB6ZnN6dHZwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYyNjk5NzEsImV4cCI6MjA2MTg0NTk3MX0.TywFD4g7rCEHaf4blMlIpclX_obKtAb_0mD1NmRH-t8"
    }

    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Supabase
            Log.d(TAG, "Initializing Supabase client")
            supabaseClient = createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Postgrest)
                install(GoTrue)
                install(Storage)
            }
            Log.d(TAG, "Supabase client initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Supabase client", e)
            Toast.makeText(
                applicationContext, 
                "Failed to initialize Supabase: ${e.message}", 
                Toast.LENGTH_LONG
            ).show()
        }
    }
} 