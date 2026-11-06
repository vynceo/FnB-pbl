package com.dadarberedar.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dadarberedar.app.data.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val slug: String,
    val type: String?,
    val icon: String?
)

fun CategoryEntity.toModel(): Category = Category(
    id = id,
    name = name,
    slug = slug,
    type = type,
    icon = icon
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    slug = slug,
    type = type,
    icon = icon
)
