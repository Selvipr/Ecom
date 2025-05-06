package com.tiruvear.textiles

import android.app.Application
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class TiruvearApp : Application() {

    companion object {
        lateinit var supabaseClient: SupabaseClient
            private set
            
        const val SUPABASE_URL = "https://spgxymonqkttpzfsztvp.supabase.co"
        const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNwZ3h5bW9ucWt0dHB6ZnN6dHZwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDYyNjk5NzEsImV4cCI6MjA2MTg0NTk3MX0.TywFD4g7rCEHaf4blMlIpclX_obKtAb_0mD1NmRH-t8"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Supabase
        supabaseClient = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Postgrest)
            install(GoTrue)
            install(Storage)
        }
    }
} 