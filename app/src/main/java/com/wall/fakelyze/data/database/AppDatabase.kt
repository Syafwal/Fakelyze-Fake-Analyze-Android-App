package com.wall.fakelyze.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wall.fakelyze.data.database.dao.HistoryDao
import com.wall.fakelyze.data.database.entity.HistoryEntity
import com.wall.fakelyze.data.database.util.Converters

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
