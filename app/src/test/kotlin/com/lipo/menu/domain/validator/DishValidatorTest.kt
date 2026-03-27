package com.lipo.menu.domain.validator

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DishValidatorTest {

    private lateinit var dishValidator: DishValidator

    @BeforeEach
    fun setup() {
        dishValidator = DishValidator()
    }

    @Test
    fun validatenameWithValidNameReturnsSuccess() {
        // Given
        val name = "Kung Pao Chicken"

        // When
        val result = dishValidator.validateName(name)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(name, result.getOrThrow())
    }

    @Test
    fun validatenameWithEmptyNameThrowsValidationexception() {
        // Given
        val name = ""

        // When & Then
        assertThrows<ValidationException> {
            dishValidator.validateName(name)
        }
    }

    @Test
    fun validatenameWithBlankNameThrowsValidationexception() {
        // Given
        val name = "   "

        // When & Then
        assertThrows<ValidationException> {
            dishValidator.validateName(name)
        }
    }

    @Test
    fun validatenameWithNameExceeding100CharsThrowsValidationexception() {
        // Given
        val name = "a".repeat(101)

        // When & Then
        assertThrows<ValidationException> {
            dishValidator.validateName(name)
        }
    }

    @Test
    fun validatedescriptionWithValidDescriptionReturnsSuccess() {
        // Given
        val description = "A spicy stir-fry dish"

        // When
        val result = dishValidator.validateDescription(description)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun validatedescriptionWithNullReturnsSuccess() {
        // Given
        val description: String? = null

        // When
        val result = dishValidator.validateDescription(description)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun validatedescriptionWithDescriptionExceeding500CharsThrowsValidationexception() {
        // Given
        val description = "a".repeat(501)

        // When & Then
        assertThrows<ValidationException> {
            dishValidator.validateDescription(description)
        }
    }
}
