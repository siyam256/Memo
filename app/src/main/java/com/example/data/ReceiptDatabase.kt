package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReceiptEntity::class, CompanySettingsEntity::class, CategoryEntity::class], version = 3, exportSchema = false)
abstract class ReceiptDatabase : RoomDatabase() {
    abstract val receiptDao: ReceiptDao

    companion object {
        @Volatile
        private var INSTANCE: ReceiptDatabase? = null

        fun getDatabase(context: Context): ReceiptDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReceiptDatabase::class.java,
                    "receipt_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
