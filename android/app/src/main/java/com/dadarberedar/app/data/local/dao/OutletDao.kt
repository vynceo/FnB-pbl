package com.dadarberedar.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dadarberedar.app.data.local.entity.OutletEntity

@Dao
interface OutletDao {
    @Query("SELECT * FROM outlets")
    suspend fun getAllOutlets(): List<OutletEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutlets(outlets: List<OutletEntity>)
}
