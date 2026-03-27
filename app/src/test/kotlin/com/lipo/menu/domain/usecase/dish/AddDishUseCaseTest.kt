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

class AddDishUseCaseTest {

    private lateinit var dishRepository: DishRepository
    private lateinit var addDishUseCase: AddDishUseCase

    @BeforeEach
    fun setup() {
        dishRepository = mockk()
        addDishUseCase = AddDishUseCase(dishRepository)
    }

    @Test
    fun invokeWithValidNameAndDescriptionReturnsSuccess() = runTest {
        // Given
        val name = "Kung Pao Chicken"
        val description = "Spicy stir-fry dish"
        val expectedDish = Dish(
            id = "test-id",
            name = name,
            description = description,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
        coEvery { dishRepository.addDish(name, description) } returns Result.success(expectedDish)

        // When
        val result = addDishUseCase(name, description)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedDish, result.getOrThrow())
    }

    @Test
    fun invokeWithRepositoryFailureReturnsFailure() = runTest {
        // Given
        val name = "Test Dish"
        val description = null
        val exception = Exception("Database error")
        coEvery { dishRepository.addDish(name, description) } returns Result.failure(exception)

        // When
        val result = addDishUseCase(name, description)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun invokeWithNullDescriptionSucceeds() = runTest {
        // Given
        val name = "Simple Dish"
        val description = null
        val expectedDish = Dish(
            id = "test-id",
            name = name,
            description = null,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
        coEvery { dishRepository.addDish(name, description) } returns Result.success(expectedDish)

        // When
        val result = addDishUseCase(name, description)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedDish, result.getOrThrow())
    }
}
