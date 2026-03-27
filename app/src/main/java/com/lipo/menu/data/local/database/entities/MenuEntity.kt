package com.lipo.menu.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "menus",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["created_at"])
    ]
)
data class MenuEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "date")
    val date: Long, // Epoch day (LocalDate.toEpochDay())

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
