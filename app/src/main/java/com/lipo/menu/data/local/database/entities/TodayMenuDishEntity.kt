package com.lipo.menu.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "today_menu_dishes",
    primaryKeys = ["today_menu_id", "dish_id"],
    foreignKeys = [
        ForeignKey(
            entity = TodayMenuEntity::class,
            parentColumns = ["id"],
            childColumns = ["today_menu_id"],
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
        Index(value = ["today_menu_id"]),
        Index(value = ["dish_id"])
    ]
)
data class TodayMenuDishEntity(
    @ColumnInfo(name = "today_menu_id")
    val todayMenuId: String,

    @ColumnInfo(name = "dish_id")
    val dishId: String,

    @ColumnInfo(name = "order")
    val order: Int
)
