package com.lipo.menu.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    private val iso8601Formatter = DateTimeFormatter.ISO_INSTANT

    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun toInstant(epochMilli: Long): Instant = Instant.ofEpochMilli(epochMilli)

    fun toEpochMilli(instant: Instant): Long = instant.toEpochMilli()

    fun formatForDisplay(date: LocalDate): String = date.format(displayDateFormatter)

    fun formatForStorage(date: LocalDate): String = date.format(dateFormatter)

    fun getCurrentInstant(): Instant = Instant.now()

    fun getCurrentLocalDate(): LocalDate = LocalDate.now()

    fun getCurrentEpochMilli(): Long = System.currentTimeMillis()

    fun getCurrentEpochDay(): Long = LocalDate.now().toEpochDay()

    /**
     * 将 Instant 转换为 ISO 8601 格式字符串（用于 Supabase）
     * 例如: 2024-01-15T10:30:00Z
     */
    fun toISO8601(instant: Instant): String {
        return iso8601Formatter.format(instant)
    }

    /**
     * 从 ISO 8601 格式字符串解析为 Instant
     */
    fun parseISO8601(isoString: String): Instant {
        return Instant.parse(isoString)
    }
}
