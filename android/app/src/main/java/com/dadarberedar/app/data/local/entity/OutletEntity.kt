package com.dadarberedar.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dadarberedar.app.data.model.Outlet

@Entity(tableName = "outlets")
data class OutletEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val slug: String,
    val city: String,
    val province: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val phone: String?,
    val whatsapp: String?,
    val openingHours: String,
    val rating: Float,
    val reviewCount: Int,
    val gofoodUrl: String?,
    val grabfoodUrl: String?,
    val shopeefoodUrl: String?,
    val gmapsUrl: String?,
    val isActive: Boolean,
    val distance: Double?
)

fun OutletEntity.toModel(): Outlet = Outlet(
    id = id,
    name = name,
    slug = slug,
    city = city,
    province = province,
    address = address,
    latitude = latitude,
    longitude = longitude,
    phone = phone,
    whatsapp = whatsapp,
    openingHours = openingHours,
    rating = rating,
    reviewCount = reviewCount,
    gofoodUrl = gofoodUrl,
    grabfoodUrl = grabfoodUrl,
    shopeefoodUrl = shopeefoodUrl,
    gmapsUrl = gmapsUrl,
    isActive = isActive,
    distance = distance
)

fun Outlet.toEntity(): OutletEntity = OutletEntity(
    id = id,
    name = name,
    slug = slug,
    city = city,
    province = province,
    address = address,
    latitude = latitude,
    longitude = longitude,
    phone = phone,
    whatsapp = whatsapp,
    openingHours = openingHours,
    rating = rating,
    reviewCount = reviewCount,
    gofoodUrl = gofoodUrl,
    grabfoodUrl = grabfoodUrl,
    shopeefoodUrl = shopeefoodUrl,
    gmapsUrl = gmapsUrl,
    isActive = isActive,
    distance = distance
)
