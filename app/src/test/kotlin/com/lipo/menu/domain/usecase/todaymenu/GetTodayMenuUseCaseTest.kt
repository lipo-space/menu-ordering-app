package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.data.model.Dish
import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetTodayMenuUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var getTodayMenuUseCase: GetTodayMenuUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        getTodayMenuUseCase = GetTodayMenuUseCase(todayMenuRepository)
    }

    @Test
    fun invokeShouldReturnTodayMenuForGivenDate() = runTest {
        // Given
        val date = LocalDate.of(2024, 3, 27)
        val expectedMenu = createTodayMenu(id = "1", date = date)
        every { todayMenuRepository.getTodayMenu(date) } returns flowOf(expectedMenu)

        // When
        val result = getTodayMenuUseCase(date).first()

        // Then
        assertTrue(result != null)
        assertEquals("1", result?.id)
        assertEquals(date, result?.date)
        verify { todayMenuRepository.getTodayMenu(date) }
    }

    @Test
    fun invokeShouldReturnNullWhenMenuNotFound() = runTest {
        // Given
        val date = LocalDate.of(2024, 3, 27)
        every { todayMenuRepository.getTodayMenu(date) } returns flowOf(null)

        // When
        val result = getTodayMenuUseCase(date).first()

        // Then
        assertNull(result)
        verify { todayMenuRepository.getTodayMenu(date) }
    }

    @Test
    fun invokeWithDefaultDateShouldUseToday() = runTest {
        // Given
        val today = LocalDate.now()
        val expectedMenu = createTodayMenu(id = "1", date = today)
        every { todayMenuRepository.getTodayMenu(today) } returns flowOf(expectedMenu)

        // When
        val result = getTodayMenuUseCase().first()

        // Then
        assertTrue(result != null)
        assertEquals(today, result?.date)
        verify { todayMenuRepository.getTodayMenu(today) }
    }

    private fun createTodayMenu(id: String, date: LocalDate): TodayMenu {
        return TodayMenu(
            id = id,
            date = date,
            dishes = emptyList(),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
