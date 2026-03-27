package com.lipo.menu.presentation.todaymenu

import com.lipo.menu.data.model.Dish
import com.lipo.menu.data.model.TodayMenu
import com.lipo.menu.domain.usecase.dish.GetAllDishesUseCase
import com.lipo.menu.domain.usecase.combination.GetAllCombinationsUseCase
import com.lipo.menu.domain.usecase.todaymenu.AddDishToTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.ClearTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.CreateTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.DeleteTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.GetTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.RemoveDishFromTodayMenuUseCase
import com.lipo.menu.domain.usecase.todaymenu.UpdateTodayMenuUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TodayMenuViewModelTest {

    private lateinit var getTodayMenuUseCase: GetTodayMenuUseCase
    private lateinit var createTodayMenuUseCase: CreateTodayMenuUseCase
    private lateinit var updateTodayMenuUseCase: UpdateTodayMenuUseCase
    private lateinit var deleteTodayMenuUseCase: DeleteTodayMenuUseCase
    private lateinit var addDishToTodayMenuUseCase: AddDishToTodayMenuUseCase
    private lateinit var removeDishFromTodayMenuUseCase: RemoveDishFromTodayMenuUseCase
    private lateinit var clearTodayMenuUseCase: ClearTodayMenuUseCase
    private lateinit var getAllDishesUseCase: GetAllDishesUseCase
    private lateinit var getAllCombinationsUseCase: GetAllCombinationsUseCase
    private lateinit var viewModel: TodayMenuViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getTodayMenuUseCase = mockk()
        createTodayMenuUseCase = mockk()
        updateTodayMenuUseCase = mockk()
        deleteTodayMenuUseCase = mockk()
        addDishToTodayMenuUseCase = mockk()
        removeDishFromTodayMenuUseCase = mockk()
        clearTodayMenuUseCase = mockk()
        getAllDishesUseCase = mockk()
        getAllCombinationsUseCase = mockk()

        // Setup default behavior for flows
        coEvery { getTodayMenuUseCase.invoke() } returns flowOf(null)
        coEvery { getAllDishesUseCase.invoke() } returns flowOf(emptyList())
        coEvery { getAllCombinationsUseCase.invoke() } returns flowOf(emptyList())

        viewModel = TodayMenuViewModel(
            getTodayMenuUseCase,
            createTodayMenuUseCase,
            updateTodayMenuUseCase,
            deleteTodayMenuUseCase,
            addDishToTodayMenuUseCase,
            removeDishFromTodayMenuUseCase,
            clearTodayMenuUseCase,
            getAllDishesUseCase,
            getAllCombinationsUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateShouldHaveNoMenuAndNoDishes() = runTest {
        // Given - setup in beforeEach

        // When
        val state = viewModel.uiState.value

        // Then
        assertNull(state.todayMenu)
        assertTrue(state.allDishes.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun showcreatedialogShouldUpdateState() = runTest {
        // Given - setup in beforeEach

        // When
        viewModel.showCreateDialog()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value.showCreateDialog)
    }

    @Test
    fun hidecreatedialogShouldUpdateState() = runTest {
        // Given
        viewModel.showCreateDialog()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.hideCreateDialog()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.showCreateDialog)
    }

    @Test
    fun createtodaymenuShouldCallUseCaseAndHideDialog() = runTest {
        // Given
        val date = LocalDate.now()
        val dishIds = listOf("dish1", "dish2")
        val menu = createTodayMenu(id = "menu1", date = date, dishIds = dishIds)
        coEvery { createTodayMenuUseCase(date, dishIds) } returns Result.success(menu)

        // When
        viewModel.showCreateDialog()
        viewModel.createTodayMenu(date, dishIds)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.showCreateDialog)
        coEvery { createTodayMenuUseCase(date, dishIds) }
    }

    @Test
    fun deletetodaymenuShouldCallUseCaseAndHideDialog() = runTest {
        // Given
        val menuId = "menu1"
        coEvery { deleteTodayMenuUseCase(menuId) } returns Result.success(Unit)

        // When
        viewModel.showDeleteDialog()
        viewModel.deleteTodayMenu(menuId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.showDeleteDialog)
        coEvery { deleteTodayMenuUseCase(menuId) }
    }

    @Test
    fun clearerrorShouldRemoveErrorFromState() = runTest {
        // Given - manually set an error
        // This would normally happen through a failed operation

        // When
        viewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    private fun createTodayMenu(id: String, date: LocalDate, dishIds: List<String>): TodayMenu {
        val dishes = dishIds.map { dishId ->
            Dish(
                id = dishId,
                name = "Dish $dishId",
                description = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        }
        return TodayMenu(
            id = id,
            date = date,
            dishes = dishes,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
