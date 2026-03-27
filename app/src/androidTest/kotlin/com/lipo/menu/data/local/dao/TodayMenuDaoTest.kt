package com.lipo.menu.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lipo.menu.data.local.database.MenuDatabase
import com.lipo.menu.data.local.database.dao.DishDao
import com.lipo.menu.data.local.database.dao.TodayMenuDao
import com.lipo.menu.data.local.database.entities.DishEntity
import com.lipo.menu.data.local.database.entities.TodayMenuDishEntity
import com.lipo.menu.data.local.database.entities.TodayMenuEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodayMenuDaoTest {

    private lateinit var database: MenuDatabase
    private lateinit var todayMenuDao: TodayMenuDao
    private lateinit var dishDao: DishDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MenuDatabase::class.java
        ).allowMainThreadQueries().build()

        todayMenuDao = database.todayMenuDao()
        dishDao = database.dishDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun "insertTodayMenu should insert menu successfully"() = runTest {
        // Given
        val menu = createTodayMenuEntity(id = "1", date = "2024-03-27")

        // When
        todayMenuDao.insertTodayMenu(menu)
        val result = todayMenuDao.getTodayMenuById("1").first()

        // Then
        assertNotNull(result)
        assertEquals("2024-03-27", result?.todayMenu?.date)
    }

    @Test
    fun "insertTodayMenu with duplicate date should abort"() = runTest {
        // Given
        val menu1 = createTodayMenuEntity(id = "1", date = "2024-03-27")
        val menu2 = createTodayMenuEntity(id = "2", date = "2024-03-27")

        // When & Then
        todayMenuDao.insertTodayMenu(menu1)
        try {
            todayMenuDao.insertTodayMenu(menu2)
            fail("Should have thrown exception for duplicate date")
        } catch (e: Exception) {
            // Expected behavior - unique constraint violation
        }
    }

    @Test
    fun "updateTodayMenu should update existing menu"() = runTest {
        // Given
        val menu = createTodayMenuEntity(id = "1", date = "2024-03-27")
        todayMenuDao.insertTodayMenu(menu)
        val updatedAt = System.currentTimeMillis()

        // When
        todayMenuDao.updateTodayMenu("1", "2024-03-28", updatedAt)
        val result = todayMenuDao.getTodayMenuById("1").first()

        // Then
        assertNotNull(result)
        assertEquals("2024-03-28", result?.todayMenu?.date)
        assertEquals(updatedAt, result?.todayMenu?.updatedAt)
    }

    @Test
    fun "deleteTodayMenu should remove menu"() = runTest {
        // Given
        val menu = createTodayMenuEntity(id = "1", date = "2024-03-27")
        todayMenuDao.insertTodayMenu(menu)

        // When
        todayMenuDao.deleteTodayMenu("1")
        val result = todayMenuDao.getTodayMenuById("1").first()

        // Then
        assertNull(result)
    }

    @Test
    fun "getTodayMenuByDate should return menu for given date"() = runTest {
        // Given
        val menu = createTodayMenuEntity(id = "1", date = "2024-03-27")
        todayMenuDao.insertTodayMenu(menu)

        // When
        val result = todayMenuDao.getTodayMenuByDate("2024-03-27").first()

        // Then
        assertNotNull(result)
        assertEquals("1", result?.todayMenu?.id)
    }

    @Test
    fun "getTodayMenuByDate should return null for non-existent date"() = runTest {
        // Given - empty database

        // When
        val result = todayMenuDao.getTodayMenuByDate("2024-03-27").first()

        // Then
        assertNull(result)
    }

    @Test
    fun "getAllHistoricalMenus should return menus before today"() = runTest {
        // Given
        val historicalMenu1 = createTodayMenuEntity(id = "1", date = "2024-03-25")
        val historicalMenu2 = createTodayMenuEntity(id = "2", date = "2024-03-26")
        val todayMenu = createTodayMenuEntity(id = "3", date = "2024-03-27")
        todayMenuDao.insertTodayMenu(historicalMenu1)
        todayMenuDao.insertTodayMenu(historicalMenu2)
        todayMenuDao.insertTodayMenu(todayMenu)

        // When
        val result = todayMenuDao.getAllHistoricalMenus("2024-03-27").first()

        // Then
        assertEquals(2, result.size)
        assertEquals("2024-03-26", result[0].todayMenu.date) // Most recent first
        assertEquals("2024-03-25", result[1].todayMenu.date)
    }

    @Test
    fun "getMenusByDateRange should return menus within range"() = runTest {
        // Given
        val menu1 = createTodayMenuEntity(id = "1", date = "2024-03-20")
        val menu2 = createTodayMenuEntity(id = "2", date = "2024-03-25")
        val menu3 = createTodayMenuEntity(id = "3", date = "2024-03-30")
        todayMenuDao.insertTodayMenu(menu1)
        todayMenuDao.insertTodayMenu(menu2)
        todayMenuDao.insertTodayMenu(menu3)

        // When
        val result = todayMenuDao.getMenusByDateRange("2024-03-20", "2024-03-27").first()

        // Then
        assertEquals(2, result.size)
        assertEquals("2024-03-25", result[0].todayMenu.date)
        assertEquals("2024-03-20", result[1].todayMenu.date)
    }

    @Test
    fun "searchMenusByDishName should return menus containing matching dishes"() = runTest {
        // Given
        val dish1 = createDishEntity(id = "d1", name = "Chicken Curry")
        val dish2 = createDishEntity(id = "d2", name = "Beef Steak")
        dishDao.insertDish(dish1)
        dishDao.insertDish(dish2)

        val menu1 = createTodayMenuEntity(id = "m1", date = "2024-03-27")
        val menu2 = createTodayMenuEntity(id = "m2", date = "2024-03-28")
        todayMenuDao.insertTodayMenu(menu1)
        todayMenuDao.insertTodayMenu(menu2)

        todayMenuDao.insertTodayMenuDish(TodayMenuDishEntity("m1", "d1", 0))
        todayMenuDao.insertTodayMenuDish(TodayMenuDishEntity("m2", "d2", 0))

        // When
        val result = todayMenuDao.searchMenusByDishName("chicken").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("m1", result[0].todayMenu.id)
    }

    @Test
    fun "insertTodayMenuDish should add dish to menu"() = runTest {
        // Given
        val menu = createTodayMenuEntity(id = "m1", date = "2024-03-27")
        val dish = createDishEntity(id = "d1", name = "Test Dish")
        todayMenuDao.insertTodayMenu(menu)
        dishDao.insertDish(dish)

        val menuDish = TodayMenuDishEntity("m1", "d1", 0)

        // When
        todayMenuDao.insertTodayMenuDish(menuDish)
        val result = todayMenuDao.getTodayMenuById("m1").first()

        // Then
        assertNotNull(result)
        assertEquals(1, result?.dishes?.size)
        assertEquals("Test Dish", result?.dishes?.get(0)?.name)
    }

    @Test
    fun "deleteTodayMenuDish should remove dish from menu"() = runTest {
        // Given
        val menu = createTodayMenuEntity(id = "m1", date = "2024-03-27")
        val dish = createDishEntity(id = "d1", name = "Test Dish")
        todayMenuDao.insertTodayMenu(menu)
        dishDao.insertDish(dish)
        todayMenuDao.insertTodayMenuDish(TodayMenuDishEntity("m1", "d1", 0))

        // When
        todayMenuDao.deleteTodayMenuDish("m1", "d1")
        val result = todayMenuDao.getTodayMenuById("m1").first()

        // Then
        assertNotNull(result)
        assertEquals(0, result?.dishes?.size)
    }

    @Test
    fun "deleteTodayMenuDishesByMenu should remove all dishes from menu"() = runTest {
        // Given
        val menu = createTodayMenuEntity(id = "m1", date = "2024-03-27")
        val dish1 = createDishEntity(id = "d1", name = "Dish 1")
        val dish2 = createDishEntity(id = "d2", name = "Dish 2")
        todayMenuDao.insertTodayMenu(menu)
        dishDao.insertDish(dish1)
        dishDao.insertDish(dish2)
        todayMenuDao.insertTodayMenuDish(TodayMenuDishEntity("m1", "d1", 0))
        todayMenuDao.insertTodayMenuDish(TodayMenuDishEntity("m1", "d2", 1))

        // When
        todayMenuDao.deleteTodayMenuDishesByMenu("m1")
        val result = todayMenuDao.getTodayMenuById("m1").first()

        // Then
        assertNotNull(result)
        assertEquals(0, result?.dishes?.size)
    }

    @Test
    fun "deleting menu should cascade delete menu dishes"() = runTest {
        // Given
        val menu = createTodayMenuEntity(id = "m1", date = "2024-03-27")
        val dish = createDishEntity(id = "d1", name = "Test Dish")
        todayMenuDao.insertTodayMenu(menu)
        dishDao.insertDish(dish)
        todayMenuDao.insertTodayMenuDish(TodayMenuDishEntity("m1", "d1", 0))

        // When
        todayMenuDao.deleteTodayMenu("m1")

        // Then - dish should still exist (RESTRICT on dish foreign key)
        val dishResult = dishDao.getDishByIdSync("d1")
        assertNotNull(dishResult)
    }

    private fun createTodayMenuEntity(
        id: String = "1",
        date: String = "2024-03-27",
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    ): TodayMenuEntity {
        return TodayMenuEntity(
            id = id,
            date = date,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun createDishEntity(
        id: String = "1",
        name: String = "Test Dish",
        description: String? = null,
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis(),
        isDeleted: Boolean = false
    ): DishEntity {
        return DishEntity(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = isDeleted
        )
    }
}
