package com.lipo.menu.ui.dish

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material3.MaterialTheme
import com.lipo.menu.data.model.Dish
import com.lipo.menu.presentation.dish.DishItem
import com.lipo.menu.presentation.dish.DeleteDishDialog
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class DishListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dishItemShouldDisplayDishName() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish", description = "Test Description")

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DishItem(
                    dish = dish,
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Dish").assertIsDisplayed()
    }

    @Test
    fun dishItemShouldDisplayDescriptionWhenPresent() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish", description = "Test Description")

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DishItem(
                    dish = dish,
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Description").assertIsDisplayed()
    }

    @Test
    fun dishItemShouldNotDisplayDescriptionWhenNull() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish", description = null)

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DishItem(
                    dish = dish,
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Dish").assertIsDisplayed()
        // Description should not exist
        composeTestRule.onNodeWithText("null").assertDoesNotExist()
    }

    @Test
    fun dishItemShouldDisplayEditButton() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish")

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DishItem(
                    dish = dish,
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
    }

    @Test
    fun dishItemShouldDisplayDeleteButton() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish")

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DishItem(
                    dish = dish,
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
    }

    @Test
    fun dishItemShouldTriggerOnEditWhenEditButtonClicked() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish")
        var editClicked = false

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DishItem(
                    dish = dish,
                    onEdit = { editClicked = true },
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        // Then
        assert(editClicked)
    }

    @Test
    fun dishItemShouldTriggerOnDeleteWhenDeleteButtonClicked() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish")
        var deleteClicked = false

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DishItem(
                    dish = dish,
                    onEdit = {},
                    onDelete = { deleteClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        // Then
        assert(deleteClicked)
    }

    @Test
    fun deleteDishDialogShouldDisplayDishName() {
        // Given
        val dish = createDish(id = "1", name = "Chicken Curry")

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DeleteDishDialog(
                    dish = dish,
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        // Then - 验证标题（没有点击动作的文本）
        composeTestRule.onNode(
            hasText("确认删除") and not(hasClickAction())
        ).assertIsDisplayed()
        composeTestRule.onNode(hasText("Chicken Curry", substring = true)).assertIsDisplayed()
    }

    @Test
    fun deleteDishDialogShouldDisplayCancelAndDeleteButtons() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish")

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DeleteDishDialog(
                    dish = dish,
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        // Then - 验证取消按钮
        composeTestRule.onNodeWithText("取消").assertIsDisplayed()
        // 验证确认删除按钮（有点击动作的文本）
        composeTestRule.onNode(
            hasText("确认删除") and hasClickAction()
        ).assertIsDisplayed()
    }

    @Test
    fun deleteDishDialogShouldTriggerOnDismissWhenCancelClicked() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish")
        var dismissClicked = false

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DeleteDishDialog(
                    dish = dish,
                    onDismiss = { dismissClicked = true },
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("取消").performClick()

        // Then
        assert(dismissClicked)
    }

    @Test
    fun deleteDishDialogShouldTriggerOnConfirmWhenDeleteClicked() {
        // Given
        val dish = createDish(id = "1", name = "Test Dish")
        var confirmClicked = false

        // When
        composeTestRule.setContent {
            MaterialTheme {
                DeleteDishDialog(
                    dish = dish,
                    onDismiss = {},
                    onConfirm = { confirmClicked = true }
                )
            }
        }

        // 使用 hasText 和 isClickable 来找到确认按钮（因为有两个"确认删除"文本）
        composeTestRule.onNode(
            hasText("确认删除") and hasClickAction()
        ).performClick()

        // Then
        assert(confirmClicked)
    }

    private fun createDish(
        id: String = "1",
        name: String = "Test Dish",
        description: String? = null
    ): Dish {
        return Dish(
            id = id,
            name = name,
            description = description,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isDeleted = false
        )
    }
}
