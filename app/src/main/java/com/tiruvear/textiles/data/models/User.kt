package com.tiruvear.textiles.data.models

import java.util.Date

data class User(
    val id: String,
    val email: String,
    val phone: String,
    val firstName: String,
    val lastName: String,
    val createdAt: Date,
    val updatedAt: Date
) 