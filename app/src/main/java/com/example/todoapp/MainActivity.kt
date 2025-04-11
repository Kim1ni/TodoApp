package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.todoapp.ui.AddTodoDialog
import com.example.todoapp.ui.TodoList
import com.example.todoapp.ui.TodoViewModel
import com.example.todoapp.ui.theme.ToDoAppTheme


class MainActivity : ComponentActivity() {
    private val todoViewModel: TodoViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoApp(todoViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp(viewModel: TodoViewModel) {
    val todos by viewModel.allTodos.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddTodoDialog(
            onDismissRequest = { showAddDialog = false },
            onConfirm = { title, description ->
                viewModel.addTodo(title, description)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todo App") },
                actions = {
                    // Only show delete all completed option when in Completed filter state
                    if (filterState == TodoViewModel.FilterState.COMPLETED && todos.isNotEmpty()) {
                        IconButton(onClick = { viewModel.deleteAllCompletedTodos() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete All Completed"
                            )
                        }
                    }
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                viewModel.setFilter(TodoViewModel.FilterState.ALL)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Active") },
                            onClick = {
                                viewModel.setFilter(TodoViewModel.FilterState.ACTIVE)
                                showFilterMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Completed") },
                            onClick = {
                                viewModel.setFilter(TodoViewModel.FilterState.COMPLETED)
                                showFilterMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Todo"
                )
            }
        },
        content = { paddingValues ->
            val filteredTodos = when (filterState) {
                TodoViewModel.FilterState.ALL -> todos
                TodoViewModel.FilterState.ACTIVE -> todos.filter { !it.isCompleted }
                TodoViewModel.FilterState.COMPLETED -> todos.filter { it.isCompleted }
            }

            TodoList(
                todos = filteredTodos,
                onToggleCompleted = { viewModel.toggleTodoCompleted(it) },
                onDelete = { viewModel.deleteTodo(it) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}