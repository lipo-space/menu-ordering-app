package com.lipo.menu.domain.validator

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TodayMenuValidatorTest {

    private lateinit var todayMenuValidator: TodayMenuValidator

    @BeforeEach
    fun setup() {
        todayMenuValidator = TodayMenuValidator()
    }

    @Test
    fun validatemenudateWithTodayReturnsSuccess() {
        // Given
        val date = LocalDate.now()

        // When & Then - should not throw exception
        todayMenuValidator.validateMenuDate(date)
    }

    @Test
    fun validatemenudateWithPastDateReturnsSuccess() {
        // Given
        val date = LocalDate.now().minusDays(5)

        // When & Then - should not throw exception
        todayMenuValidator.validateMenuDate(date)
    }

    @Test
    fun validatemenudateWithTomorrowReturnsSuccess() {
        // Given
        val date = LocalDate.now().plusDays(1)

        // When & Then - should not throw exception
        todayMenuValidator.validateMenuDate(date)
    }

    @Test
    fun validatemenudateWithDateBeyond1DayInFutureThrowsValidationexception() {
        // Given
        val date = LocalDate.now().plusDays(2)

        // When & Then
        assertThrows<ValidationException> {
            todayMenuValidator.validateMenuDate(date)
        }
    }

    @Test
    fun validatedishselectionWithValidDishListReturnsSuccess() {
        // Given
        val dishIds = listOf("dish1", "dish2", "dish3")

        // When & Then - should not throw exception
        todayMenuValidator.validateDishSelection(dishIds)
    }

    @Test
    fun validatedishselectionWithSingleDishReturnsSuccess() {
        // Given
        val dishIds = listOf("dish1")

        // When & Then - should not throw exception
        todayMenuValidator.validateDishSelection(dishIds)
    }

    @Test
    fun validatedishselectionWithEmptyListThrowsValidationexception() {
        // Given
        val dishIds = emptyList<String>()

        // When & Then
        assertThrows<ValidationException> {
            todayMenuValidator.validateDishSelection(dishIds)
        }
    }

    @Test
    fun validatedishselectionWithDuplicateDishesThrowsValidationexception() {
        // Given
        val dishIds = listOf("dish1", "dish2", "dish1")

        // When & Then
        assertThrows<ValidationException> {
            todayMenuValidator.validateDishSelection(dishIds)
        }
    }

    @Test
    fun validatedishselectionWithBlankDishIdThrowsValidationexception() {
        // Given
        val dishIds = listOf("dish1", "", "dish2")

        // When & Then
        assertThrows<ValidationException> {
            todayMenuValidator.validateDishSelection(dishIds)
        }
    }
}
