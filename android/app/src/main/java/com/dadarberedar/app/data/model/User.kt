package com.dadarberedar.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?
)

data class AuthResponse(
    val message: String,
    val token: String,
    val user: User
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    val phone: String? = null
)
