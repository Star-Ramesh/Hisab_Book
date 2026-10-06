package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [KhataTransaction::class, KhataSettings::class],
  version = 2,
  exportSchema = false
)
abstract class KhataDatabase : RoomDatabase() {
  abstract fun khataDao(): KhataDao

  companion object {
    @Volatile
    private var INSTANCE: KhataDatabase? = null

    fun getDatabase(context: Context): KhataDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          KhataDatabase::class.java,
          "my_khata_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
