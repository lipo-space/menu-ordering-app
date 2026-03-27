package com.lipo.menu.data.model

import java.time.Instant

data class MenuEntry(
    val menuId: String,
    val dishId: String,
    val order: Int,
    val quantity: Int = 1,
    val addedAt: Instant,
    val dish: Dish? = null
)
