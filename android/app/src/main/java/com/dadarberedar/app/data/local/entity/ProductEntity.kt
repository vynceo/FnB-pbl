package com.dadarberedar.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dadarberedar.app.data.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val categoryId: Long,
    val name: String,
    val slug: String,
    val description: String?,
    val price: Double,
    val spicinessLevel: Int,
    val isCrispy: Boolean,
    val isBestseller: Boolean,
    val isSignature: Boolean,
    val imageUrl: String?,
    val type: String,
    val stock: Int,
    val isAvailable: Boolean
)

fun ProductEntity.toModel(): Product = Product(
    id = id,
    categoryId = categoryId,
    name = name,
    slug = slug,
    description = description,
    price = price,
    spicinessLevel = spicinessLevel,
    isCrispy = isCrispy,
    isBestseller = isBestseller,
    isSignature = isSignature,
    imageUrl = imageUrl,
    type = type,
    stock = stock,
    isAvailable = isAvailable,
    category = null
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    categoryId = categoryId,
    name = name,
    slug = slug,
    description = description,
    price = price,
    spicinessLevel = spicinessLevel,
    isCrispy = isCrispy,
    isBestseller = isBestseller,
    isSignature = isSignature,
    imageUrl = imageUrl,
    type = type,
    stock = stock,
    isAvailable = isAvailable
)
