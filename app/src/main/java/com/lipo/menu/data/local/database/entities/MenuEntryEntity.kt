package com.lipo.menu.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "menu_entries",
    primaryKeys = ["menu_id", "dish_id", "order"],
    foreignKeys = [
        ForeignKey(
            entity = MenuEntity::class,
            parentColumns = ["id"],
            childColumns = ["menu_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dish_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["menu_id"]),
        Index(value = ["dish_id"]),
        Index(value = ["menu_id", "order"], unique = true)
    ]
)
data class MenuEntryEntity(
    @ColumnInfo(name = "menu_id")
    val menuId: String,

    @ColumnInfo(name = "dish_id")
    val dishId: String,

    @ColumnInfo(name = "order")
    val order: Int,

    @ColumnInfo(name = "quantity")
    val quantity: Int = 1,

    @ColumnInfo(name = "added_at")
    val addedAt: Long
)
