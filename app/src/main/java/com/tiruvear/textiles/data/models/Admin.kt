package com.tiruvear.textiles.data.models

import java.util.Date

data class Admin(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: Date,
    val updatedAt: Date
) 