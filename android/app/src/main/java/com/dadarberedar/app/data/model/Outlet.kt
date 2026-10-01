package com.dadarberedar.app.data.model

import com.google.gson.annotations.SerializedName

data class Outlet(
    val id: Long,
    val name: String,
    val slug: String,
    val city: String,
    val province: String = "Indonesia",
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val phone: String?,
    val whatsapp: String?,
    @SerializedName("opening_hours") val openingHours: String = "10.00 - 22.00 WIB",
    val rating: Float = 4.9f,
    @SerializedName("review_count") val reviewCount: Int = 500,
    @SerializedName("gofood_url") val gofoodUrl: String?,
    @SerializedName("grabfood_url") val grabfoodUrl: String?,
    @SerializedName("shopeefood_url") val shopeefoodUrl: String?,
    @SerializedName("gmaps_url") val gmapsUrl: String?,
    @SerializedName("is_active") val isActive: Boolean = true,
    val distance: Double? = null
)
