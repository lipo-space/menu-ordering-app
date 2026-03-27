package com.lipo.menu.data.model

import java.time.Instant
import java.time.LocalDate

data class Menu(
    val id: String,
    val date: LocalDate,
    val createdAt: Instant,
    val updatedAt: Instant,
    val dishes: List<MenuEntry> = emptyList()
)
