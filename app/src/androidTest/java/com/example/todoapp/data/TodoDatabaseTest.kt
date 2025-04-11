package com.example.todoapp.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoDatabaseTest {
    private lateinit var todoDao: TodoDao
    private lateinit var db: TodoDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TodoDatabase::class.java).build()
        todoDao = db.todoDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetTodo() = runBlocking {
        // Given
        val todo = Todo(title = "Test Todo")

        // When
        val id = todoDao.insertTodo(todo)
        val loadedTodo = todoDao.getTodoById(id)

        // Then
        assertEquals("Test Todo", loadedTodo?.title)
    }

    @Test
    fun updateTodo() = runBlocking {
        // Given
        val todo = Todo(title = "Original Title")
        val id = todoDao.insertTodo(todo)

        // When
        val loadedTodo = todoDao.getTodoById(id)
        val updatedTodo = loadedTodo?.copy(title = "Updated Title")
        if (updatedTodo != null) {
            todoDao.updateTodo(updatedTodo)
        }

        // Then
        val reloadedTodo = todoDao.getTodoById(id)
        assertEquals("Updated Title", reloadedTodo?.title)
    }

    @Test
    fun deleteTodo() = runBlocking {
        // Given
        val todo = Todo(title = "Todo to delete")
        val id = todoDao.insertTodo(todo)

        // When
        val loadedTodo = todoDao.getTodoById(id)
        if (loadedTodo != null) {
            todoDao.deleteTodo(loadedTodo)
        }

        // Then
        val deletedTodo = todoDao.getTodoById(id)
        assertNull(deletedTodo)
    }

    @Test
    fun getActiveTodos() = runBlocking {
        // Given
        todoDao.insertTodo(Todo(title = "Active 1"))
        todoDao.insertTodo(Todo(title = "Active 2"))
        todoDao.insertTodo(Todo(title = "Completed", isCompleted = true))

        // When
        val activeTodos = todoDao.getActiveTodos().first()

        // Then
        assertEquals(2, activeTodos.size)
        assertEquals("Active 1", activeTodos[0].title)
        assertEquals("Active 2", activeTodos[1].title)
    }

    @Test
    fun getCompletedTodos() = runBlocking {
        // Given
        todoDao.insertTodo(Todo(title = "Active"))
        todoDao.insertTodo(Todo(title = "Completed 1", isCompleted = true))
        todoDao.insertTodo(Todo(title = "Completed 2", isCompleted = true))

        // When
        val completedTodos = todoDao.getCompletedTodos().first()

        // Then
        assertEquals(2, completedTodos.size)
    }

    @Test
    fun deleteAllCompletedTodos() = runBlocking {
        // Given
        todoDao.insertTodo(Todo(title = "Active"))
        todoDao.insertTodo(Todo(title = "Completed 1", isCompleted = true))
        todoDao.insertTodo(Todo(title = "Completed 2", isCompleted = true))

        // When
        todoDao.deleteAllCompletedTodos()
        val allTodos = todoDao.getAllTodos().first()

        // Then
        assertEquals(1, allTodos.size)
        assertEquals("Active", allTodos[0].title)
    }
}