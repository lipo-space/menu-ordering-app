package com.lipo.menu.domain.usecase.dish

import com.lipo.menu.data.model.Dish
import com.lipo.menu.domain.repository.DishRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SearchDishesUseCaseTest {

    private lateinit var dishRepository: DishRepository
    private lateinit var searchDishesUseCase: SearchDishesUseCase

    @BeforeEach
    fun setup() {
        dishRepository = mockk()
        searchDishesUseCase = SearchDishesUseCase(dishRepository)
    }

    @Test
    fun invokeWithQueryReturnsMatchingDishes() = runTest {
        // Given
        val query = "chicken"
        val expectedDishes = listOf(
            Dish(
                id = "1",
                name = "Kung Pao Chicken",
                description = "Spicy",
                createdAt = java.time.Instant.now(),
                updatedAt = java.time.Instant.now()
            ),
            Dish(
                id = "2",
                name = "Sweet and Sour Chicken",
                description = "Tangy",
                createdAt = java.time.Instant.now(),
                updatedAt = java.time.Instant.now()
            )
        )
        every { dishRepository.searchDishes(query) } returns flowOf(expectedDishes)

        // When
        val result = searchDishesUseCase(query)

        // Then
        result.collect { dishes ->
            assertEquals(expectedDishes, dishes)
        }
    }

    @Test
    fun invokeWithEmptyQueryReturnsEmptyList() = runTest {
        // Given
        val query = ""
        every { dishRepository.searchDishes(query) } returns flowOf(emptyList())

        // When
        val result = searchDishesUseCase(query)

        // Then
        result.collect { dishes ->
            assertEquals(emptyList(), dishes)
        }
    }
}
