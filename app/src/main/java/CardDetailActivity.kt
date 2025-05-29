package com.example.listsqre_revamped

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.listsqre_revamped.ui.CardAppTheme

class CardDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cardTitle = intent.getStringExtra("CARD_TITLE") ?: "Untitled"
        val cardId = intent.getLongExtra("CARD_ID", -1L)
        if (cardId == -1L) finish()

        setContent {
            CardAppTheme {
                val app = LocalContext.current.applicationContext as MyApplication
                val viewModel = remember {
                    CardItemViewModel(app.database.cardItemDao())
                }
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
    viewModel: CardItemViewModel = viewModel()
) {
    val context = LocalContext.current
    val cardItems by viewModel.itemsForCard.collectAsState()
    val isLoading by viewModel.isLoadingForItems.collectAsState()
    var showDropdown by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CardItem?>(null) }
    val selectedItems = remember { mutableStateListOf<Long>() }

    LaunchedEffect(cardId) {
        viewModel.setCardId(cardId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = cardTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showDropdown = true }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Pin up selected") },
                                onClick = {
                                    viewModel.setPinnedForItems(cardId, selectedItems.toList(), true)
                                    selectedItems.clear()
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete selected") },
                                onClick = {
                                    viewModel.deleteItemsByIds(cardId, selectedItems.toList())
                                    selectedItems.clear()
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            )
        },floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.defaultMinSize(
                    minWidth = 56.dp,
                    minHeight = 56.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },

        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Please wait...",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 88.dp /* padding 56 + 16 + 16 */
                ),
            ) {
                items(cardItems, key = { it.id }) { item ->
                    CardDetailItem(
                        item = item,
                        isSelected = selectedItems.contains(item.id),
                        onCheckedChange = { checked ->
                            if (checked) selectedItems.add(item.id)
                            else selectedItems.remove(item.id)
                        },
                        onClick = {
                            if (item.description.isValidUrl()) {
                                val intent = Intent(Intent.ACTION_VIEW, item.description.toUri())
                                context.startActivity(intent)
                            }
                        },
                        onEditClick = { editingItem = item },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = {description, isPinned ->
                val newItem = CardItem(
                    cardId = cardId,
                    description = description,
                    isPinned = isPinned
                )
                viewModel.insertCardItem(newItem)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { editingItem = null },
            onSave = {description, isPinned ->
                val updated = item.copy(
                    description = description,
                    isPinned = isPinned
                )
                viewModel.updateCardItem(updated)
                editingItem = null
            }
        )
    }
}

// Update AddCardDialog to include pin checkbox
@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    singleLine = false
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
                onClick = { onConfirm(description, isPinned) },
                enabled = description.isNotBlank()
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

@Composable
fun EditItemDialog(
    item: CardItem,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var description by remember { mutableStateOf(item.description) }
    var isPinned by remember { mutableStateOf(item.isPinned) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder  = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    singleLine = false
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
                onClick = { onSave(description, isPinned) },
                enabled = description.isNotBlank()
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

@Composable
fun CardDetailItem(
    item: CardItem,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onCheckedChange(it) },
            modifier = Modifier.clickable { onCheckedChange(!item.isSelected) }
        )
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (item.isPinned) FontWeight.W900 else FontWeight.Normal
            ),
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .combinedClickable(
                    onClick = { onClick() },
                    onLongClick = {
                        onEditClick()
                    }
                )
        )
    }
}

/* URL validity check, only for card items */
fun String.isValidUrl(): Boolean {
    return try {
        val uri = this.toUri()
        uri.scheme?.startsWith("http") == true
    } catch (_: Exception) {
        false
    }
}