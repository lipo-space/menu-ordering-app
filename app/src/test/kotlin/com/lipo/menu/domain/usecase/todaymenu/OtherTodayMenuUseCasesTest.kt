package com.lipo.menu.domain.usecase.todaymenu

import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.repository.TodayMenuRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertTrue

class DeleteTodayMenuUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var deleteTodayMenuUseCase: DeleteTodayMenuUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        deleteTodayMenuUseCase = DeleteTodayMenuUseCase(todayMenuRepository)
    }

    @Test
    fun invokeShouldDeleteMenu() = runTest {
        // Given
        val id = "menu1"
        coEvery { todayMenuRepository.deleteTodayMenu(id) } returns Result.success(Unit)

        // When
        val result = deleteTodayMenuUseCase(id)

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
        val result = deleteTodayMenuUseCase(id)

        // Then
        assertTrue(result.isFailure)
    }
}

class AddDishToTodayMenuUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var addDishToTodayMenuUseCase: AddDishToTodayMenuUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        addDishToTodayMenuUseCase = AddDishToTodayMenuUseCase(todayMenuRepository)
    }

    @Test
    fun invokeShouldAddDishToMenu() = runTest {
        // Given
        val menuId = "menu1"
        val dishId = "dish1"
        val order = 0
        coEvery { todayMenuRepository.addDishToTodayMenu(menuId, dishId, order) } returns Result.success(Unit)

        // When
        val result = addDishToTodayMenuUseCase(menuId, dishId, order)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todayMenuRepository.addDishToTodayMenu(menuId, dishId, order) }
    }
}

class RemoveDishFromTodayMenuUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var removeDishFromTodayMenuUseCase: RemoveDishFromTodayMenuUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        removeDishFromTodayMenuUseCase = RemoveDishFromTodayMenuUseCase(todayMenuRepository)
    }

    @Test
    fun invokeShouldRemoveDishFromMenu() = runTest {
        // Given
        val menuId = "menu1"
        val dishId = "dish1"
        coEvery { todayMenuRepository.removeDishFromTodayMenu(menuId, dishId) } returns Result.success(Unit)

        // When
        val result = removeDishFromTodayMenuUseCase(menuId, dishId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todayMenuRepository.removeDishFromTodayMenu(menuId, dishId) }
    }
}

class ClearTodayMenuUseCaseTest {

    private lateinit var todayMenuRepository: TodayMenuRepository
    private lateinit var clearTodayMenuUseCase: ClearTodayMenuUseCase

    @BeforeEach
    fun setup() {
        todayMenuRepository = mockk()
        clearTodayMenuUseCase = ClearTodayMenuUseCase(todayMenuRepository)
    }

    @Test
    fun invokeShouldClearAllDishesFromMenu() = runTest {
        // Given
        val date = LocalDate.now()
        val menu = createTodayMenu(id = "menu1", date = date)
        every { todayMenuRepository.getTodayMenu(date) } returns flowOf(menu)
        coEvery { todayMenuRepository.updateTodayMenu("menu1", date, emptyList()) } returns Result.success(menu)

        // When
        val result = clearTodayMenuUseCase(date)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todayMenuRepository.updateTodayMenu("menu1", date, emptyList()) }
    }

    @Test
    fun invokeWithNoExistingMenuShouldSucceed() = runTest {
        // Given
        val date = LocalDate.now()
        every { todayMenuRepository.getTodayMenu(date) } returns flowOf(null)

        // When
        val result = clearTodayMenuUseCase(date)

        // Then
        assertTrue(result.isSuccess)
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
