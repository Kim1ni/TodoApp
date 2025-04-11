package com.example.todoapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Todo
import com.example.todoapp.data.TodoDatabase
import com.example.todoapp.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository
    val allTodos: StateFlow<List<Todo>>

    // Filter state
    private val _filterState = MutableStateFlow(FilterState.ALL)
    val filterState: StateFlow<FilterState> = _filterState

    init {
        val todoDao = TodoDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)
        allTodos = combine(
            _filterState,
            repository.allTodos,
            repository.activeTodos,
            repository.completedTodos
        ) { filter, all, active, completed ->
            when (filter) {
                FilterState.ALL -> all
                FilterState.ACTIVE -> active
                FilterState.COMPLETED -> completed
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun addTodo(title: String, description: String = "") {
        viewModelScope.launch {
            repository.insertTodo(title, description)
        }
    }

    fun toggleTodoCompleted(todo: Todo) {
        viewModelScope.launch {
            repository.toggleTodoCompleted(todo)
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch {
            repository.updateTodo(todo)
        }
    }

    fun deleteAllCompletedTodos() {
        viewModelScope.launch {
            repository.deleteAllCompletedTodos()
        }
    }

    suspend fun getTodoById(id: Long): Todo? {
        return repository.getTodoById(id)
    }

    fun setFilter(filter: FilterState) {
        _filterState.value = filter
    }

    enum class FilterState {
        ALL, ACTIVE, COMPLETED
    }
}