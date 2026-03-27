package com.lipo.menu.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun toInstant(epochMilli: Long): Instant = Instant.ofEpochMilli(epochMilli)

    fun toEpochMilli(instant: Instant): Long = instant.toEpochMilli()

    fun formatForDisplay(date: LocalDate): String = date.format(displayDateFormatter)

    fun formatForStorage(date: LocalDate): String = date.format(dateFormatter)

    fun getCurrentInstant(): Instant = Instant.now()

    fun getCurrentLocalDate(): LocalDate = LocalDate.now()

    fun getCurrentEpochMilli(): Long = System.currentTimeMillis()

    fun getCurrentEpochDay(): Long = LocalDate.now().toEpochDay()
}
