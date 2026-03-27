package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.domain.repository.DishRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DeleteDishUseCaseTest {

    private lateinit var dishRepository: DishRepository
    private lateinit var deleteDishUseCase: DeleteDishUseCase

    @BeforeEach
    fun setup() {
        dishRepository = mockk()
        deleteDishUseCase = DeleteDishUseCase(dishRepository)
    }

    @Test
    fun invokeWithValidIdReturnsSuccess() = runTest {
        // Given
        val id = "dish-123"
        coEvery { dishRepository.deleteDish(id) } returns Result.success(Unit)

        // When
        val result = deleteDishUseCase(id)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun invokeWithNonExistentIdReturnsFailure() = runTest {
        // Given
        val id = "non-existent"
        val exception = Exception("Dish not found")
        coEvery { dishRepository.deleteDish(id) } returns Result.failure(exception)

        // When
        val result = deleteDishUseCase(id)

        // Then
        assertTrue(result.isFailure)
    }
}
