package com.wall.fakelyze.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wall.fakelyze.data.database.dao.HistoryDao
import com.wall.fakelyze.data.database.entity.HistoryEntity
import com.wall.fakelyze.data.database.util.Converters

@Database(entities = [HistoryEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        // Migrasi dari versi 1 ke versi 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Menambahkan kolom explanation ke tabel history
                database.execSQL("ALTER TABLE history ADD COLUMN explanation TEXT")
            }
        }
    }
}
