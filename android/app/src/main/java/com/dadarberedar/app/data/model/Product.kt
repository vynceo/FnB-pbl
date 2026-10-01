package com.dadarberedar.app.data.model

import com.dadarberedar.app.AppConfig
import com.google.gson.annotations.SerializedName

data class Category(
    val id: Long,
    val name: String,
    val slug: String,
    val type: String?,
    val icon: String?
)

data class Product(
    val id: Long,
    @SerializedName("category_id") val categoryId: Long,
    val name: String,
    val slug: String,
    val description: String?,
    val price: Double,
    @SerializedName("spiciness_level") val spicinessLevel: Int = 0,
    @SerializedName("is_crispy") val isCrispy: Boolean = false,
    @SerializedName("is_bestseller") val isBestseller: Boolean = false,
    @SerializedName("is_signature") val isSignature: Boolean = false,
    @SerializedName("image_url") val imageUrl: String?,
    val type: String = "food",
    val stock: Int = 100,
    @SerializedName("is_available")     val isAvailable: Boolean = true,
    val category: Category? = null
)

val Product.resolvedImageUrl: String?
    get() = imageUrl?.let {
        when {
            it.startsWith("http://") || it.startsWith("https://") -> it
            it.startsWith("/") -> AppConfig.BASE_URL.trimEnd('/') + it
            else -> AppConfig.BASE_URL + it
        }
    }
