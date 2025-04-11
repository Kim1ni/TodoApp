package com.example.todoapp.repository

import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDao
import kotlinx.coroutines.flow.Flow
import java.util.Date

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<Todo>> = todoDao.getAllTodos()
    val activeTodos: Flow<List<Todo>> = todoDao.getActiveTodos()
    val completedTodos: Flow<List<Todo>> = todoDao.getCompletedTodos()

    suspend fun insertTodo(title: String, description: String = ""): Long {
        val todo = Todo(
            title = title,
            description = description
        )
        return todoDao.insertTodo(todo)
    }

    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }

    suspend fun toggleTodoCompleted(todo: Todo) {
        val updatedTodo = if (todo.isCompleted) {
            todo.copy(isCompleted = false, completedDate = null)
        } else {
            todo.copy(isCompleted = true, completedDate = Date())
        }
        todoDao.updateTodo(updatedTodo)
    }

    suspend fun deleteAllCompletedTodos() {
        todoDao.deleteAllCompletedTodos()
    }

    suspend fun getTodoById(id: Long): Todo? {
        return todoDao.getTodoById(id)
    }
}