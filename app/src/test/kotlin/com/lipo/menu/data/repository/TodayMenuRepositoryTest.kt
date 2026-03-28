package com.lipo.menu.data.repository

import com.lipo.menu.data.local.database.dao.TodayMenuDao
import com.lipo.menu.data.local.database.entities.DishEntity
import com.lipo.menu.data.local.database.entities.TodayMenuDishEntity
import com.lipo.menu.data.local.database.entities.TodayMenuEntity
import com.lipo.menu.data.local.database.entities.TodayMenuWithDishes
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class TodayMenuRepositoryTest {

    private lateinit var todayMenuDao: TodayMenuDao
    private lateinit var repository: TodayMenuRepositoryImpl

    @Before
    fun setup() {
        todayMenuDao = mockk()
        repository = TodayMenuRepositoryImpl(todayMenuDao)
    }

    @Test
    fun gettodaymenuShouldReturnMenuForGivenDate() = runTest {
        // Given
        val date = LocalDate.of(2024, 3, 27)
        val entity = createTodayMenuWithDishes(id = "1", date = "2024-03-27")
        every { todayMenuDao.getTodayMenuByDate("2024-03-27") } returns flowOf(entity)

        // When
        val result = repository.getTodayMenu(date).first()

        // Then
        assertNotNull(result)
        assertEquals("1", result?.id)
        assertEquals(date, result?.date)
    }

    @Test
    fun gettodaymenuShouldReturnNullWhenMenuNotFound() = runTest {
        // Given
        val date = LocalDate.of(2024, 3, 27)
        every { todayMenuDao.getTodayMenuByDate("2024-03-27") } returns flowOf(null)

        // When
        val result = repository.getTodayMenu(date).first()

        // Then
        assertNull(result)
    }

    @Test
    fun gettodaymenubyidShouldReturnMenuForGivenId() = runTest {
        // Given
        val entity = createTodayMenuWithDishes(id = "1", date = "2024-03-27")
        every { todayMenuDao.getTodayMenuById("1") } returns flowOf(entity)

        // When
        val result = repository.getTodayMenuById("1").first()

        // Then
        assertNotNull(result)
        assertEquals("1", result?.id)
    }

    @Test
    fun gethistoricalmenusShouldReturnMenusBeforeToday() = runTest {
        // Given
        val entities = listOf(
            createTodayMenuWithDishes(id = "1", date = "2024-03-25"),
            createTodayMenuWithDishes(id = "2", date = "2024-03-26")
        )
        every { todayMenuDao.getAllHistoricalMenus() } returns flowOf(entities)

        // When
        val result = repository.getHistoricalMenus().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }

    @Test
    fun getmenusbydaterangeShouldReturnMenusWithinRange() = runTest {
        // Given
        val startDate = LocalDate.of(2024, 3, 20)
        val endDate = LocalDate.of(2024, 3, 27)
        val entities = listOf(
            createTodayMenuWithDishes(id = "1", date = "2024-03-25"),
            createTodayMenuWithDishes(id = "2", date = "2024-03-26")
        )
        every { todayMenuDao.getMenusByDateRange("2024-03-20", "2024-03-27") } returns flowOf(entities)

        // When
        val result = repository.getMenusByDateRange(startDate, endDate).first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun searchhistoricalmenusShouldReturnMenusWithMatchingDishes() = runTest {
        // Given
        val query = "chicken"
        val entities = listOf(
            createTodayMenuWithDishes(id = "1", date = "2024-03-27")
        )
        every { todayMenuDao.searchMenusByDishName("%chicken%") } returns flowOf(entities)

        // When
        val result = repository.searchHistoricalMenus(query).first()

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun createtodaymenuShouldCreateMenuWithDishes() = runTest {
        // Given
        val date = LocalDate.of(2024, 3, 27)
        val dishIds = listOf("d1", "d2")
        val createdEntity = createTodayMenuWithDishes(id = "new-id", date = "2024-03-27")

        coJustRun { todayMenuDao.insertTodayMenu(any()) }
        coJustRun { todayMenuDao.insertTodayMenuDish(any()) }
        every { todayMenuDao.getTodayMenuById(any()) } returns flowOf(createdEntity)

        // When
        val result = repository.createTodayMenu(date, dishIds)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todayMenuDao.insertTodayMenu(any()) }
        coVerify(exactly = 2) { todayMenuDao.insertTodayMenuDish(any()) }
    }

    @Test
    fun updatetodaymenuShouldUpdateMenuAndReplaceDishes() = runTest {
        // Given
        val id = "1"
        val date = LocalDate.of(2024, 3, 27)
        val dishIds = listOf("d1", "d2")
        val updatedEntity = createTodayMenuWithDishes(id = id, date = "2024-03-27")

        coJustRun { todayMenuDao.updateTodayMenu(id, "2024-03-27", any()) }
        coJustRun { todayMenuDao.deleteTodayMenuDishesByMenu(id) }
        coJustRun { todayMenuDao.insertTodayMenuDish(any()) }
        every { todayMenuDao.getTodayMenuById(id) } returns flowOf(updatedEntity)

        // When
        val result = repository.updateTodayMenu(id, date, dishIds)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todayMenuDao.updateTodayMenu(id, "2024-03-27", any()) }
        coVerify { todayMenuDao.deleteTodayMenuDishesByMenu(id) }
        coVerify(exactly = 2) { todayMenuDao.insertTodayMenuDish(any()) }
    }

    @Test
    fun deletetodaymenuShouldDeleteMenu() = runTest {
        // Given
        val id = "1"
        coJustRun { todayMenuDao.deleteTodayMenu(id) }

        // When
        val result = repository.deleteTodayMenu(id)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todayMenuDao.deleteTodayMenu(id) }
    }

    @Test
    fun adddishtotodaymenuShouldAddDish() = runTest {
        // Given
        val menuId = "m1"
        val dishId = "d1"
        val order = 0
        coJustRun { todayMenuDao.insertTodayMenuDish(any()) }

        // When
        val result = repository.addDishToTodayMenu(menuId, dishId, order)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            todayMenuDao.insertTodayMenuDish(match {
                it.todayMenuId == menuId && it.dishId == dishId && it.order == order
            })
        }
    }

    @Test
    fun removedishfromtodaymenuShouldRemoveDish() = runTest {
        // Given
        val menuId = "m1"
        val dishId = "d1"
        coJustRun { todayMenuDao.deleteTodayMenuDish(menuId, dishId) }

        // When
        val result = repository.removeDishFromTodayMenu(menuId, dishId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { todayMenuDao.deleteTodayMenuDish(menuId, dishId) }
    }

    @Test
    fun toDomainModelShouldFilterOutSoftDeletedDishes() = runTest {
        // Given
        val entity = createTodayMenuWithDishes(
            id = "1",
            date = "2024-03-27",
            dishes = listOf(
                createDishEntity(id = "d1", name = "Active Dish", isDeleted = false),
                createDishEntity(id = "d2", name = "Deleted Dish", isDeleted = true)
            )
        )
        every { todayMenuDao.getTodayMenuByDate("2024-03-27") } returns flowOf(entity)

        // When
        val result = repository.getTodayMenu(LocalDate.of(2024, 3, 27)).first()

        // Then
        assertNotNull(result)
        assertEquals(1, result?.dishes?.size)
        assertEquals("Active Dish", result?.dishes?.get(0)?.name)
    }

    private fun createTodayMenuWithDishes(
        id: String = "1",
        date: String = "2024-03-27",
        dishes: List<DishEntity> = emptyList()
    ): TodayMenuWithDishes {
        return TodayMenuWithDishes(
            todayMenu = TodayMenuEntity(
                id = id,
                date = date,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            dishes = dishes
        )
    }

    private fun createDishEntity(
        id: String = "1",
        name: String = "Test Dish",
        isDeleted: Boolean = false
    ): DishEntity {
        return DishEntity(
            id = id,
            name = name,
            description = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDeleted = isDeleted
        )
    }
}
