package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.repository.DishRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateDishUseCaseTest {

    private lateinit var dishRepository: DishRepository
    private lateinit var updateDishUseCase: UpdateDishUseCase

    @BeforeEach
    fun setup() {
        dishRepository = mockk()
        updateDishUseCase = UpdateDishUseCase(dishRepository)
    }

    @Test
    fun invokeWithValidDataReturnsSuccess() = runTest {
        // Given
        val id = "dish-123"
        val name = "Updated Dish"
        val description = "Updated description"
        val expectedDish = Dish(
            id = id,
            name = name,
            description = description,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
        coEvery { dishRepository.updateDish(id, name, description) } returns Result.success(expectedDish)

        // When
        val result = updateDishUseCase(id, name, description)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedDish, result.getOrThrow())
    }

    @Test
    fun invokeWithNonExistentIdReturnsFailure() = runTest {
        // Given
        val id = "non-existent"
        val name = "Updated Dish"
        val description = "Description"
        val exception = Exception("Dish not found")
        coEvery { dishRepository.updateDish(id, name, description) } returns Result.failure(exception)

        // When
        val result = updateDishUseCase(id, name, description)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
