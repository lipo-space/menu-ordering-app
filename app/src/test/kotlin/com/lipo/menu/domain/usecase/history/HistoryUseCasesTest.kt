package com.lipo.menu.domain.usecase.history

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import kotlin.test.assertTrue

class GetHistoricalMenusUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var getHistoricalMenusUseCase: GetHistoricalMenusUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        getHistoricalMenusUseCase = GetHistoricalMenusUseCase(todayMenuRepository)
    }

    @Test
    fun invokeShouldReturnHistoricalMenus() = runTest {
        // Given
        val historicalMenus = listOf(
            createTodayMenu(id = "1", date = LocalDate.of(2024, 3, 25)),
            createTodayMenu(id = "2", date = LocalDate.of(2024, 3, 26))
        )
        every { todayMenuRepository.getHistoricalMenus() } returns flowOf(historicalMenus)

        // When
        val result = getHistoricalMenusUseCase().first()

        // Then
        assertEquals(2, result.size)
        verify { todayMenuRepository.getHistoricalMenus() }
    }

    @Test
    fun invokeWithEmptyHistoryReturnsEmptyList() = runTest {
        // Given
        every { todayMenuRepository.getHistoricalMenus() } returns flowOf(emptyList())

        // When
        val result = getHistoricalMenusUseCase().first()

        // Then
        assertTrue(result.isEmpty())
    }
}

class GetMenusByDateRangeUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var getMenusByDateRangeUseCase: GetMenusByDateRangeUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        getMenusByDateRangeUseCase = GetMenusByDateRangeUseCase(todayMenuRepository)
    }

    @Test
    fun invokeShouldReturnMenusWithinDateRange() = runTest {
        // Given
        val startDate = LocalDate.of(2024, 3, 20)
        val endDate = LocalDate.of(2024, 3, 27)
        val menus = listOf(
            createTodayMenu(id = "1", date = LocalDate.of(2024, 3, 25)),
            createTodayMenu(id = "2", date = LocalDate.of(2024, 3, 26))
        )
        every { todayMenuRepository.getMenusByDateRange(startDate, endDate) } returns flowOf(menus)

        // When
        val result = getMenusByDateRangeUseCase(startDate, endDate).first()

        // Then
        assertEquals(2, result.size)
        verify { todayMenuRepository.getMenusByDateRange(startDate, endDate) }
    }
}

class SearchHistoricalMenusUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var searchHistoricalMenusUseCase: SearchHistoricalMenusUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        searchHistoricalMenusUseCase = SearchHistoricalMenusUseCase(todayMenuRepository)
    }

    @Test
    fun invokeShouldReturnMatchingMenus() = runTest {
        // Given
        val query = "chicken"
        val menus = listOf(
            createTodayMenu(id = "1", date = LocalDate.of(2024, 3, 27))
        )
        every { todayMenuRepository.searchHistoricalMenus(query) } returns flowOf(menus)

        // When
        val result = searchHistoricalMenusUseCase(query).first()

        // Then
        assertEquals(1, result.size)
        verify { todayMenuRepository.searchHistoricalMenus(query) }
    }

    @Test
    fun invokeWithBlankQueryReturnsEmptyList() = runTest {
        // Given
        val query = ""
        every { todayMenuRepository.searchHistoricalMenus(query) } returns flowOf(emptyList())

        // When
        val result = searchHistoricalMenusUseCase(query).first()

        // Then
        assertTrue(result.isEmpty())
    }
}

class DeleteHistoricalMenuUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var deleteHistoricalMenuUseCase: DeleteHistoricalMenuUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        deleteHistoricalMenuUseCase = DeleteHistoricalMenuUseCase(todayMenuRepository)
    }

    @Test
    fun invokeShouldDeleteMenu() = runTest {
        // Given
        val id = "menu1"
        coEvery { todayMenuRepository.deleteTodayMenu(id) } returns Result.success(Unit)

        // When
        val result = deleteHistoricalMenuUseCase(id)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todayMenuRepository.deleteTodayMenu(id) }
    }

    @Test
    fun invokeWithRepositoryFailureReturnsFailure() = runTest {
        // Given
        val id = "menu1"
        val exception = Exception("Database error")
        coEvery { todayMenuRepository.deleteTodayMenu(id) } returns Result.failure(exception)

        // When
        val result = deleteHistoricalMenuUseCase(id)

        // Then
        assertTrue(result.isFailure)
    }
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
