package com.lipo.menu.data.model

import java.time.Instant
import java.time.LocalDate

data class TodayMenu(
    val id: String,
    val date: LocalDate,
    val dishes: List<Dish>,
    val createdAt: Instant,
    val updatedAt: Instant
)
