package com.tiruvear.textiles.data.models

import java.util.Date

data class Address(
    val id: String,
    val userId: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val state: String,
    val pincode: String,
    val isDefault: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 