package com.srabbijan.sheetdb.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.srabbijan.sheetdb.data.local.entity.DataItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onSignInClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val items by viewModel.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (uiState.isSignedIn) {
            SignedInContent(
                userEmail = uiState.userEmail,
                items = items,
                isSyncing = uiState.isSyncing,
                syncMessage = uiState.syncMessage,
                onAddItem = viewModel::addItem,
                onSync = viewModel::syncData,
                onSignOut = viewModel::signOut
            )
        } else {
            SignInContent(onSignInClick = onSignInClick)
        }
    }
}

@Composable
private fun SignInContent(onSignInClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign in with Google to continue",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSignInClick) {
            Text("Sign In with Google")
        }
    }
}

@Composable
private fun SignedInContent(
    userEmail: String?,
    items: List<DataItem>,
    isSyncing: Boolean,
    syncMessage: String?,
    onAddItem: (String, String) -> Unit,
    onSync: () -> Unit,
    onSignOut: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Welcome, $userEmail",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onSignOut) {
                Text("Sign Out")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Item")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSync,
                enabled = !isSyncing,
                modifier = Modifier.weight(1f)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Sync")
                }
            }
        }

        syncMessage?.let {
            Text(
                text = it,
                color = if (it.contains("success")) Color.Green else Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Items list
        LazyColumn {
            items(items) { item ->
                ItemCard(item = item)
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, description ->
                onAddItem(title, description)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ItemCard(
    item: DataItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (!item.isSynced) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Not synced",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Updated: ${
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(Date(item.updatedAt))
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, description)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}