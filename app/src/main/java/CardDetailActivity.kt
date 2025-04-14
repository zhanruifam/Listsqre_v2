package com.example.listsqre_revamped

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.listsqre_revamped.ui.CardAppTheme

class CardDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cardTitle = intent.getStringExtra("CARD_TITLE") ?: "Card Details"

        setContent {
            CardAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CardDetailAppScreen(cardTitle = cardTitle)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailAppScreen(cardTitle: String) {
    val context = LocalContext.current
    var showDropdown by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<Card?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$cardTitle Items") },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SpotlightActivity::class.java))
                    }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List")
                    }
                    Box {
                        IconButton(onClick = { showDropdown = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Pin selected") },
                                onClick = {
                                    // TODO: Add functionality for pinning selected items
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete selected") },
                                onClick = {
                                    // TODO: Add functionality for deleting selected items
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) { /* TODO: Add functionality for displaying card details */ }
    }

    if (showAddDialog) {
        AddCardDetailDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, isPinned ->
                // TODO: Add functionality for adding new card
                showAddDialog = false
            }
        )
    }

    editingCard?.let { card ->
        EditCardDetailDialog(
            card = card,
            onDismiss = { editingCard = null },
            onSave = { title, description, isPinned ->
                // TODO: Add functionality for saving edited card
                editingCard = null
            }
        )
    }
}

// Update AddCardDialog to include pin checkbox
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardDetailDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Card") },
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
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it }
                    )
                    Text("Pin to top of list")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description, isPinned) },
                enabled = title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardDetailDialog(
    card: Card,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(card.title) }
    var description by remember { mutableStateOf(card.description) }
    var isPinned by remember { mutableStateOf(card.isPinned) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Card") },
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
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it }
                    )
                    Text("Pin to top of list")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, description, isPinned) },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailItem(
    card: Card,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isPinned) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = card.isSelected,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.clickable { }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
            ) {
                Column {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            IconButton(
                onClick = { onEditClick() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit"
                )
            }
        }
    }
}