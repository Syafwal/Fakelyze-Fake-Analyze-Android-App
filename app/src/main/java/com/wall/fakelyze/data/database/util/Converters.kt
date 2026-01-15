package com.wall.fakelyze.data.database.util

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return try {
            value?.let { Date(it) }
        } catch (e: Exception) {
            null // Return null instead of crashing
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return try {
            date?.time
        } catch (e: Exception) {
            null // Return null instead of crashing
        }
    }
}
