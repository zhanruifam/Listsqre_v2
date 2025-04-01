package com.example.listsqre_revamped

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
        val cardId = intent.getLongExtra("CARD_ID", 0L)

        setContent {
            /* TODO: further optimization needed */
            val app = LocalContext.current.applicationContext as MyApplication
            val context = LocalContext.current
            val db_ = remember { AppDatabaseCardField.getDatabase(context) }
            val viewModel = remember { DynamicTableManager(app.database, db_) }

            CardAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CardDetailAppScreen(
                        cardTitle = cardTitle,
                        cardId = cardId,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailAppScreen(
    cardTitle: String,
    cardId: Long,
    viewModel: DynamicTableManager
) {
    val context = LocalContext.current
    var showDropdown by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingCardItem by remember { mutableStateOf<CardField?>(null) }
    var cardFields by remember { mutableStateOf<List<CardField>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load data when screen first appears or when cardId changes
    LaunchedEffect(cardId) {
        viewModel.getAllFields(cardId) { fields ->
            cardFields = fields
            isLoading = false
        }
    }

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
                                    // Pin all selected items
                                    cardFields.filter { it.isSelected }.forEach { field ->
                                        viewModel.toggleFieldPin(cardId, field.id) {
                                            viewModel.getAllFields(cardId) { updatedFields ->
                                                cardFields = updatedFields
                                            }
                                        }
                                    }
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete selected") },
                                onClick = {
                                    // Delete all selected items
                                    cardFields.filter { it.isSelected }.forEach { field ->
                                        viewModel.deleteField(cardId, field.id) {
                                            viewModel.getAllFields(cardId) { updatedFields ->
                                                cardFields = updatedFields
                                            }
                                        }
                                    }
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
                onClick = { showAddItemDialog = true },
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
        ) {
            items(cardFields.filter { it.isPinned }) { card ->
                CardFieldItem(
                    card = card,
                    onCheckedChange = { isChecked ->
                        // viewModel.toggleFieldSelection(card.id, isChecked)
                    },
                    onClick = {
                        /* TODO: further optimization needed */
                    },
                    onEditClick = { editingCardItem = card },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(cardFields.filter { !it.isPinned }) { card ->
                CardFieldItem(
                    card = card,
                    onCheckedChange = { isChecked ->
                        // viewModel.toggleFieldSelection(card.id, isChecked)
                    },
                    onClick = {
                        /* TODO: further optimization needed */
                    },
                    onEditClick = { editingCardItem = card },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }

    if (showAddItemDialog) {
        AddCardItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { title, description, isPinned ->
                val newField = CardField(
                    fieldTitle = title,
                    fieldDescription = description,
                    isPinned = isPinned
                )
                viewModel.insertField(cardId, newField) { rowId ->
                    if (rowId != -1L) {
                        viewModel.getAllFields(cardId) { updatedFields ->
                            cardFields = updatedFields
                        }
                    }
                }
                showAddItemDialog = false
            }
        )
    }

    editingCardItem?.let { field ->
        EditCardItemDialog(
            field = field,
            onDismiss = { editingCardItem = null },
            onSave = { title, description, isPinned ->
                val updatedField = field.copy(
                    fieldTitle = title,
                    fieldDescription = description,
                    isPinned = isPinned
                )
                viewModel.updateField(cardId, updatedField) {
                    viewModel.getAllFields(cardId) { updatedFields ->
                        cardFields = updatedFields
                    }
                }
                editingCardItem = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title*") },
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
                    Text("Pin to top")
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
fun EditCardItemDialog(
    field: CardField,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(field.fieldTitle) }
    var description by remember { mutableStateOf(field.fieldDescription) }
    var isPinned by remember { mutableStateOf(field.isPinned) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title*") },
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
                    Text("Pin to top")
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
fun CardFieldItem(
    card: CardField,
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
                        text = card.fieldTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.fieldDescription,
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