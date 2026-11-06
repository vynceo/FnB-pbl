package com.dadarberedar.app.di

import android.content.Context
import androidx.room.Room
import com.dadarberedar.app.data.local.AppDatabase
import com.dadarberedar.app.data.local.dao.CategoryDao
import com.dadarberedar.app.data.local.dao.OutletDao
import com.dadarberedar.app.data.local.dao.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dadar_beredar_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideOutletDao(database: AppDatabase): OutletDao = database.outletDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()
}
