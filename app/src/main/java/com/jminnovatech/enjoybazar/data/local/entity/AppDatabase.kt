package com.jminnovatech.joymart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jminnovatech.joymart.data.local.entity.ProductEntity

@Database(entities = [ProductEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
