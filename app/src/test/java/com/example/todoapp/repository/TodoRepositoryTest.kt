package com.example.todoapp.repository

import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDao
import kotlinx.coroutines.runBlocking
//import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
//import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
//import org.mockito.Mockito.`when`
import org.mockito.kotlin.argThat
import java.util.Date

class TodoRepositoryTest {
    private lateinit var todoDao: TodoDao
    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        todoDao = mock(TodoDao::class.java)
        repository = TodoRepository(todoDao)
    }
/*
    @Test
    fun insertTodo_callsDao() {
        runBlocking {
            // Given
            val title = "Test Todo"
            val description = "Test Description"
            `when`(todoDao.insertTodo(any())).thenReturn(1L)

            // When
            val result = repository.insertTodo(title, description)

            // Then
            assertEquals(1L, result)
            //verify(todoDao).insertTodo(any())
        }
    }
*/
    @Test
    fun toggleTodoCompleted_updatesCompletionStatus() {
        runBlocking {
            // Given
            val date = Date()
            val todo = Todo(id = 1, title = "Test", createdDate = date)

            // When
            repository.toggleTodoCompleted(todo)

            // Then
            verify(todoDao).updateTodo(argThat { updatedTodo ->
                updatedTodo.id == 1L && updatedTodo.isCompleted && updatedTodo.completedDate != null
            })
        }
    }
/*
    @Test
    fun toggleTodoCompleted_undoesCompletionStatus() {
        runBlocking {
            // Given
            val date = Date()
            val todo = Todo(id = 1, title = "Test", createdDate = date, isCompleted = true, completedDate = date)

            // When
            repository.toggleTodoCompleted(todo)

            // Then
            verify(todoDao).updateTodo(argThat { updatedTodo ->
                updatedTodo.id == 1L && !updatedTodo.isCompleted && updatedTodo.completedDate == null
            })
        }
    }
*/
    @Test
    fun deleteAllCompletedTodos_callsDao() {
        runBlocking {
            // When
            repository.deleteAllCompletedTodos()

            // Then
            verify(todoDao).deleteAllCompletedTodos()
        }
    }
}