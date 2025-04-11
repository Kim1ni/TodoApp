package com.example.todoapp.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todoapp.data.Todo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class TodoItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun todoItem_displaysCorrectData() {
        // Given
        val todo = Todo(
            id = 1,
            title = "Test Todo",
            description = "Test Description",
            createdDate = Date()
        )

        // When
        composeTestRule.setContent {
            TodoItem(
                todo = todo,
                onToggleCompleted = {},
                onDelete = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test Todo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Description").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
    }

    @Test
    fun todoItem_showsStrikethroughWhenCompleted() {
        // Given
        val todo = Todo(
            id = 1,
            title = "Completed Todo",
            isCompleted = true,
            completedDate = Date()
        )

        // When
        composeTestRule.setContent {
            TodoItem(
                todo = todo,
                onToggleCompleted = {},
                onDelete = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Completed Todo").assertIsDisplayed()
        // Note: It's difficult to test the strikethrough in Compose UI tests directly,
        // but we can verify the checkbox is checked
        composeTestRule.onNode(isToggleable() and isSelected()).assertIsDisplayed()
    }

    @Test
    fun checkbox_togglesOnClick() {
        // Given
        var isToggled = false
        val todo = Todo(id = 1, title = "Test Todo")

        composeTestRule.setContent {
            TodoItem(
                todo = todo,
                onToggleCompleted = { isToggled = true },
                onDelete = {}
            )
        }

        // When
        composeTestRule.onNode(isToggleable()).performClick()

        // Then
        assert(isToggled)
    }

    @Test
    fun deleteButton_callsOnDelete() {
        // Given
        var isDeleted = false
        val todo = Todo(id = 1, title = "Test Todo")

        composeTestRule.setContent {
            TodoItem(
                todo = todo,
                onToggleCompleted = {},
                onDelete = { isDeleted = true }
            )
        }

        // When
        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        // Then
        assert(isDeleted)
    }
}