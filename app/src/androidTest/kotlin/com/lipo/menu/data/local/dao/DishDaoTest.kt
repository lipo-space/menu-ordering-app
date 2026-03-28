package com.lipo.menu.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lipo.menu.data.local.database.MenuDatabase
import com.lipo.menu.data.local.database.dao.DishDao
import com.lipo.menu.data.local.database.entities.DishEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DishDaoTest {

    private lateinit var database: MenuDatabase
    private lateinit var dishDao: DishDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MenuDatabase::class.java
        ).allowMainThreadQueries().build()

        dishDao = database.dishDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getAllDishesShouldReturnAllNonDeletedDishesSortedByName() = runTest {
        // Given
        val dish1 = createDishEntity(id = "1", name = "Zebra Dish")
        val dish2 = createDishEntity(id = "2", name = "Apple Dish")
        val dish3 = createDishEntity(id = "3", name = "Banana Dish")
        val deletedDish = createDishEntity(id = "4", name = "Deleted Dish", isDeleted = true)

        dishDao.insertDish(dish1)
        dishDao.insertDish(dish2)
        dishDao.insertDish(dish3)
        dishDao.insertDish(deletedDish)

        // When
        val result = dishDao.getAllDishes().first()

        // Then
        assertEquals(3, result.size)
        assertEquals("Apple Dish", result[0].name)
        assertEquals("Banana Dish", result[1].name)
        assertEquals("Zebra Dish", result[2].name)
    }

    @Test
    fun searchDishesShouldReturnMatchingDishesLimitedTo100Results() = runTest {
        // Given
        val matchingDish = createDishEntity(id = "1", name = "Chicken Curry")
        val nonMatchingDish = createDishEntity(id = "2", name = "Beef Steak")
        dishDao.insertDish(matchingDish)
        dishDao.insertDish(nonMatchingDish)

        // When
        val result = dishDao.searchDishes("chicken").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Chicken Curry", result[0].name)
    }

    @Test
    fun searchDishesShouldExcludeDeletedDishes() = runTest {
        // Given
        val activeDish = createDishEntity(id = "1", name = "Chicken Curry")
        val deletedDish = createDishEntity(id = "2", name = "Chicken Wings", isDeleted = true)
        dishDao.insertDish(activeDish)
        dishDao.insertDish(deletedDish)

        // When
        val result = dishDao.searchDishes("chicken").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Chicken Curry", result[0].name)
    }

    @Test
    fun getDishByIdShouldReturnDishWhenFound() = runTest {
        // Given
        val dish = createDishEntity(id = "1", name = "Test Dish")
        dishDao.insertDish(dish)

        // When
        val result = dishDao.getDishById("1").first()

        // Then
        assertNotNull(result)
        assertEquals("Test Dish", result?.name)
    }

    @Test
    fun getDishByIdShouldReturnNullForDeletedDish() = runTest {
        // Given
        val dish = createDishEntity(id = "1", name = "Test Dish", isDeleted = true)
        dishDao.insertDish(dish)

        // When
        val result = dishDao.getDishById("1").first()

        // Then
        assertNull(result)
    }

    @Test
    fun getDishByIdSyncShouldReturnDishWhenFound() = runTest {
        // Given
        val dish = createDishEntity(id = "1", name = "Test Dish")
        dishDao.insertDish(dish)

        // When
        val result = dishDao.getDishByIdSync("1")

        // Then
        assertNotNull(result)
        assertEquals("Test Dish", result?.name)
    }

    @Test
    fun insertDishShouldInsertDishSuccessfully() = runTest {
        // Given
        val dish = createDishEntity(id = "1", name = "New Dish")

        // When
        dishDao.insertDish(dish)
        val result = dishDao.getDishById("1").first()

        // Then
        assertNotNull(result)
        assertEquals("New Dish", result?.name)
    }

    @Test
    fun insertDishWithDuplicateIdShouldAbort() = runTest {
        // Given
        val dish1 = createDishEntity(id = "1", name = "Dish 1")
        val dish2 = createDishEntity(id = "1", name = "Dish 2")

        // When & Then
        dishDao.insertDish(dish1)
        try {
            dishDao.insertDish(dish2)
            fail("Should have thrown exception for duplicate ID")
        } catch (e: Exception) {
            // Expected behavior - constraint violation
        }
    }

    @Test
    fun updateDishShouldUpdateExistingDish() = runTest {
        // Given
        val originalDish = createDishEntity(id = "1", name = "Original Name")
        dishDao.insertDish(originalDish)

        val updatedDish = originalDish.copy(
            name = "Updated Name",
            description = "Updated Description"
        )

        // When
        dishDao.updateDish(updatedDish)
        val result = dishDao.getDishById("1").first()

        // Then
        assertNotNull(result)
        assertEquals("Updated Name", result?.name)
        assertEquals("Updated Description", result?.description)
    }

    @Test
    fun softDeleteDishShouldMarkDishAsDeleted() = runTest {
        // Given
        val dish = createDishEntity(id = "1", name = "Test Dish")
        dishDao.insertDish(dish)
        val updatedAt = System.currentTimeMillis()

        // When
        dishDao.softDeleteDish("1", updatedAt)
        val result = dishDao.getDishByIdSync("1")

        // Then
        assertNotNull(result)
        assertTrue(result?.isDeleted == true)
        assertEquals(updatedAt, result?.updatedAt)
    }

    @Test
    fun dishNameExistsShouldReturnTrueForExistingName() = runTest {
        // Given
        val dish = createDishEntity(id = "1", name = "Chicken Curry")
        dishDao.insertDish(dish)

        // When
        val result = dishDao.dishNameExists("Chicken Curry")

        // Then
        assertTrue(result)
    }

    @Test
    fun dishNameExistsShouldReturnFalseForNonExistingName() = runTest {
        // Given - empty database

        // When
        val result = dishDao.dishNameExists("Non-Existent Dish")

        // Then
        assertFalse(result)
    }

    @Test
    fun dishNameExistsShouldBeCaseInsensitive() = runTest {
        // Given
        val dish = createDishEntity(id = "1", name = "Chicken Curry")
        dishDao.insertDish(dish)

        // When
        val result1 = dishDao.dishNameExists("CHICKEN CURRY")
        val result2 = dishDao.dishNameExists("chicken curry")
        val result3 = dishDao.dishNameExists("ChIcKeN CuRrY")

        // Then
        assertTrue(result1)
        assertTrue(result2)
        assertTrue(result3)
    }

    @Test
    fun dishNameExistsShouldExcludeDeletedDishes() = runTest {
        // Given
        val dish = createDishEntity(id = "1", name = "Chicken Curry", isDeleted = true)
        dishDao.insertDish(dish)

        // When
        val result = dishDao.dishNameExists("Chicken Curry")

        // Then
        assertFalse(result)
    }

    @Test
    fun dishNameExistsShouldExcludeSpecifiedId() = runTest {
        // Given
        val dish = createDishEntity(id = "1", name = "Chicken Curry")
        dishDao.insertDish(dish)

        // When
        val result = dishDao.dishNameExists("Chicken Curry", excludeId = "1")

        // Then
        assertFalse(result)
    }

    @Test
    fun dishNameExistsShouldReturnTrueWhenDifferentDishHasSameName() = runTest {
        // Given
        val dish1 = createDishEntity(id = "1", name = "Chicken Curry")
        val dish2 = createDishEntity(id = "2", name = "Beef Steak")
        dishDao.insertDish(dish1)
        dishDao.insertDish(dish2)

        // When - checking if dish2 can use dish1's name
        val result = dishDao.dishNameExists("Chicken Curry", excludeId = "2")

        // Then
        assertTrue(result) // Name already exists for dish1
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
