package com.dadarberedar.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dadarberedar.app.data.local.dao.CategoryDao
import com.dadarberedar.app.data.local.dao.OutletDao
import com.dadarberedar.app.data.local.dao.ProductDao
import com.dadarberedar.app.data.local.entity.CategoryEntity
import com.dadarberedar.app.data.local.entity.OutletEntity
import com.dadarberedar.app.data.local.entity.ProductEntity

@Database(entities = [OutletEntity::class, CategoryEntity::class, ProductEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun outletDao(): OutletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
}
