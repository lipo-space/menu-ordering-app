package com.lipo.menu.data.local.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): java.time.Instant? {
        return value?.let { java.time.Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: java.time.Instant?): Long? {
        return date?.toEpochMilli()
    }

    @TypeConverter
    fun fromEpochDay(value: Long?): java.time.LocalDate? {
        return value?.let { java.time.LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToEpochDay(date: java.time.LocalDate?): Long? {
        return date?.toEpochDay()
    }
}
