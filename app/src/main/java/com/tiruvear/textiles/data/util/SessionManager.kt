package com.tiruvear.textiles.data.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tiruvear.textiles.data.models.CartItem
import com.tiruvear.textiles.data.models.User

/**
 * SessionManager handles remembering user session data and preferences
 * Uses EncryptedSharedPreferences for storing sensitive information
 */
class SessionManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "TiruvearTextilesPref"
        private const val ENC_PREF_NAME = "TiruvearTextilesEncPref"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_CART_ITEMS = "cart_items"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    }
    
    private val appContext = context.applicationContext
    private val sharedPreferences: SharedPreferences = appContext.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    
    private val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPreferences = EncryptedSharedPreferences.create(
        appContext,
        ENC_PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val gson = Gson()
    
    // Authentication functions
    
    fun setUserLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun setRememberMe(remember: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_REMEMBER_ME, remember).apply()
    }
    
    fun isRememberMe(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }
    
    fun saveUserId(userId: String) {
        encryptedPreferences.edit().putString(KEY_USER_ID, userId).apply()
    }
    
    fun getUserId(): String? {
        return encryptedPreferences.getString(KEY_USER_ID, null)
    }
    
    fun saveUserToken(token: String) {
        encryptedPreferences.edit().putString(KEY_USER_TOKEN, token).apply()
    }
    
    fun getUserToken(): String? {
        return encryptedPreferences.getString(KEY_USER_TOKEN, null)
    }
    
    fun saveUserData(user: User) {
        val userJson = gson.toJson(user)
        encryptedPreferences.edit().putString(KEY_USER_DATA, userJson).apply()
    }
    
    fun getUserData(): User? {
        val userJson = encryptedPreferences.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }
    
    fun clearUserSession() {
        encryptedPreferences.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_TOKEN)
            .remove(KEY_USER_DATA)
            .apply()
        
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
    
    // Cart functions
    
    fun saveCartItems(cartItems: List<CartItem>) {
        val cartJson = gson.toJson(cartItems)
        sharedPreferences.edit().putString(KEY_CART_ITEMS, cartJson).apply()
    }
    
    fun getCartItems(): List<CartItem> {
        val cartJson = sharedPreferences.getString(KEY_CART_ITEMS, null)
        return if (cartJson != null) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson(cartJson, type)
        } else {
            emptyList()
        }
    }
    
    fun clearCart() {
        sharedPreferences.edit().remove(KEY_CART_ITEMS).apply()
    }
    
    // App preferences
    
    fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_THEME_MODE, enabled).apply()
    }
    
    fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_THEME_MODE, false)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }
    
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }
} 