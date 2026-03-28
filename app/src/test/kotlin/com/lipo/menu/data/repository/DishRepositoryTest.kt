package com.lipo.menu.data.repository

import com.lipo.menu.data.local.database.dao.DishDao
import com.lipo.menu.data.local.database.entities.DishEntity
import com.lipo.menu.data.model.Dish
import com.lipo.menu.data.remote.DishRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

class DishRepositoryTest {

    private lateinit var dishDao: DishDao
    private lateinit var remoteDataSource: DishRemoteDataSource
    private lateinit var repository: DishRepositoryImpl

    @Before
    fun setup() {
        dishDao = mockk()
        remoteDataSource = mockk(relaxed = true)  // relaxed = true 会为所有方法提供默认实现
        repository = DishRepositoryImpl(dishDao, remoteDataSource)
    }

    @Test
    fun getalldishesShouldReturnMappedDishes() = runTest {
        // Given
        val entities = listOf(
            createDishEntity(id = "1", name = "Dish 1"),
            createDishEntity(id = "2", name = "Dish 2")
        )
        every { dishDao.getAllDishes() } returns flowOf(entities)

        // When
        val result = repository.getAllDishes().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Dish 1", result[0].name)
        assertEquals("Dish 2", result[1].name)
        verify { dishDao.getAllDishes() }
    }

    @Test
    fun searchdishesWithValidQueryShouldReturnMatchingDishes() = runTest {
        // Given
        val query = "chicken"
        val entities = listOf(
            createDishEntity(id = "1", name = "Chicken Curry")
        )
        every { dishDao.searchDishes(query) } returns flowOf(entities)

        // When
        val result = repository.searchDishes(query).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Chicken Curry", result[0].name)
        verify { dishDao.searchDishes(query) }
    }

    @Test
    fun searchdishesWithBlankQueryShouldReturnAllDishes() = runTest {
        // Given
        val entities = listOf(
            createDishEntity(id = "1", name = "Dish 1")
        )
        every { dishDao.searchDishes("%") } returns flowOf(entities)

        // When
        val result = repository.searchDishes("   ").first()

        // Then
        assertEquals(1, result.size)
        verify { dishDao.searchDishes("%") }
    }

    @Test
    fun getdishbyidShouldReturnDishWhenFound() = runTest {
        // Given
        val entity = createDishEntity(id = "1", name = "Test Dish")
        every { dishDao.getDishById("1") } returns flowOf(entity)

        // When
        val result = repository.getDishById("1").first()

        // Then
        assertNotNull(result)
        assertEquals("Test Dish", result?.name)
        verify { dishDao.getDishById("1") }
    }

    @Test
    fun getdishbyidShouldReturnNullWhenNotFound() = runTest {
        // Given
        every { dishDao.getDishById("999") } returns flowOf(null)

        // When
        val result = repository.getDishById("999").first()

        // Then
        assertNull(result)
        verify { dishDao.getDishById("999") }
    }

    @Test
    fun adddishWithUniqueNameShouldSucceed() = runTest {
        // Given
        val name = "New Dish"
        val description = "Description"
        coEvery { dishDao.dishNameExists(name, null) } returns false
        coJustRun { dishDao.insertDish(any()) }
        coJustRun { remoteDataSource.upsertDish(any()) }

        // When
        val result = repository.addDish(name, description)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.name)
        assertEquals(description, result.getOrNull()?.description)
        coVerify { dishDao.insertDish(any()) }
        coVerify { remoteDataSource.upsertDish(any()) }
    }

    @Test
    fun adddishWithDuplicateNameShouldFail() = runTest {
        // Given
        val name = "Existing Dish"
        coEvery { dishDao.dishNameExists(name, null) } returns true

        // When
        val result = repository.addDish(name, null)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already exists") == true)
        coVerify(exactly = 0) { dishDao.insertDish(any()) }
    }

    @Test
    fun adddishShouldTrimNameAndDescription() = runTest {
        // Given
        val name = "  Dish Name  "
        val description = "  Description  "
        coEvery { dishDao.dishNameExists("Dish Name", null) } returns false
        coJustRun { dishDao.insertDish(any()) }
        coJustRun { remoteDataSource.upsertDish(any()) }

        // When
        val result = repository.addDish(name, description)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Dish Name", result.getOrNull()?.name)
        assertEquals("Description", result.getOrNull()?.description)
    }

    @Test
    fun updatedishWithValidDataShouldSucceed() = runTest {
        // Given
        val id = "1"
        val name = "Updated Dish"
        val description = "Updated Description"
        val existingEntity = createDishEntity(id = id, name = "Old Name")

        coEvery { dishDao.dishNameExists(name, id) } returns false
        coEvery { dishDao.getDishByIdSync(id) } returns existingEntity
        coJustRun { dishDao.updateDish(any()) }
        coJustRun { remoteDataSource.upsertDish(any()) }

        // When
        val result = repository.updateDish(id, name, description)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrNull()?.name)
        assertEquals(description, result.getOrNull()?.description)
        coVerify { dishDao.updateDish(any()) }
        coVerify { remoteDataSource.upsertDish(any()) }
    }

    @Test
    fun updatedishWithDuplicateNameShouldFail() = runTest {
        // Given
        val id = "1"
        val name = "Existing Dish"
        coEvery { dishDao.dishNameExists(name, id) } returns true

        // When
        val result = repository.updateDish(id, name, null)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already exists") == true)
        coVerify(exactly = 0) { dishDao.updateDish(any()) }
    }

    @Test
    fun updateDishWithNonExistentIdShouldFail() = runTest {
        // Given
        val id = "999"
        val name = "Updated Dish"
        coEvery { dishDao.dishNameExists(name, id) } returns false
        coEvery { dishDao.getDishByIdSync(id) } returns null

        // When
        val result = repository.updateDish(id, name, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Dish not found", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dishDao.updateDish(any()) }
    }

    @Test
    fun deletedishWithValidIdShouldSucceed() = runTest {
        // Given
        val id = "1"
        coJustRun { dishDao.softDeleteDish(eq(id), any()) }
        coJustRun { remoteDataSource.deleteDish(id) }

        // When
        val result = repository.deleteDish(id)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dishDao.softDeleteDish(eq(id), any()) }
        coVerify { remoteDataSource.deleteDish(id) }
    }

    @Test
    fun dishnameexistsShouldDelegateToDao() = runTest {
        // Given
        val name = "Test Dish"
        val excludeId = "123"
        coEvery { dishDao.dishNameExists(name, excludeId) } returns true

        // When
        val result = repository.dishNameExists(name, excludeId)

        // Then
        assertTrue(result)
        coVerify { dishDao.dishNameExists(name, excludeId) }
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
