package com.wall.fakelyze.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wall.fakelyze.data.database.dao.HistoryDao
import com.wall.fakelyze.data.database.entity.HistoryEntity
import com.wall.fakelyze.data.database.util.Converters

@Database(
    entities = [HistoryEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        private const val DATABASE_NAME = "fakelyze_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // ✅ Migration dari versi 1 ke 2 (jika ada metadata)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tambah kolom metadata jika upgrade dari v1
                database.execSQL("ALTER TABLE history ADD COLUMN metadata TEXT")
            }
        }

        // ✅ Migration dari versi 2 ke 3 (hapus kolom metadata)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Buat tabel baru tanpa kolom metadata
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS history_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        imagePath TEXT NOT NULL,
                        thumbnailPath TEXT NOT NULL,
                        isAIGenerated INTEGER NOT NULL,
                        confidenceScore REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        explanation TEXT
                    )
                """.trimIndent())

                // Copy data dari tabel lama ke tabel baru
                database.execSQL("""
                    INSERT INTO history_new (id, imagePath, thumbnailPath, isAIGenerated, confidenceScore, timestamp, explanation)
                    SELECT id, imagePath, thumbnailPath, isAIGenerated, confidenceScore, timestamp, explanation
                    FROM history
                """.trimIndent())

                // Hapus tabel lama
                database.execSQL("DROP TABLE history")

                // Rename tabel baru
                database.execSQL("ALTER TABLE history_new RENAME TO history")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
