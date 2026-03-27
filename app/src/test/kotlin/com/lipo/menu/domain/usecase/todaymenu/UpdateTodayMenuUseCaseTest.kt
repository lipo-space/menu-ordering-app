package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import com.lipo.menu.domain.validator.TodayMenuValidator
import com.lipo.menu.domain.validator.ValidationException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateTodayMenuUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var todayMenuValidator: TodayMenuValidator
    private lateinit var updateTodayMenuUseCase: UpdateTodayMenuUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        todayMenuValidator = TodayMenuValidator()
        updateTodayMenuUseCase = UpdateTodayMenuUseCase(todayMenuRepository, todayMenuValidator)
    }

    @Test
    fun invokeWithValidDataReturnsSuccess() = runTest {
        // Given
        val id = "menu1"
        val date = LocalDate.now()
        val dishIds = listOf("dish1", "dish2")
        val expectedMenu = createTodayMenu(id = id, date = date)
        coEvery { todayMenuRepository.updateTodayMenu(id, date, dishIds) } returns Result.success(expectedMenu)

        // When
        val result = updateTodayMenuUseCase(id, date, dishIds)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedMenu, result.getOrThrow())
        coVerify { todayMenuRepository.updateTodayMenu(id, date, dishIds) }
    }

    @Test
    fun invokeWithEmptyDishListReturnsFailure() = runTest {
        // Given
        val id = "menu1"
        val date = LocalDate.now()
        val dishIds = emptyList<String>()

        // When
        val result = updateTodayMenuUseCase(id, date, dishIds)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }

    @Test
    fun invokeWithRepositoryFailureReturnsFailure() = runTest {
        // Given
        val id = "menu1"
        val date = LocalDate.now()
        val dishIds = listOf("dish1")
        val exception = Exception("Database error")
        coEvery { todayMenuRepository.updateTodayMenu(id, date, dishIds) } returns Result.failure(exception)

        // When
        val result = updateTodayMenuUseCase(id, date, dishIds)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
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
