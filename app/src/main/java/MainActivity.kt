package com.example.listsqre_revamped

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.listsqre_revamped.ui.CardAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    /* Definition of permission launcher needs to be done at the class level */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(
                    this,
                    "Permission denied, enable it from settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionRequest()
        setContent {
            CardAppTheme {
                val app = LocalContext.current.applicationContext as MyApplication
                val viewModel = remember {
                    CardViewModel(app.database.cardDao())
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CardAppScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun permissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = NotificationReceiver.PERMISSION
            val permissionState = ContextCompat.checkSelfPermission(this, permission)

            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardAppScreen(viewModel: CardViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val isLoading by viewModel.isLoadingForList.collectAsState()
    var showDropdown by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<Card?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onRemindersClick = {
                    context.startActivity(Intent(context, NotificationActivity::class.java))
                },
                onThemesClick = { /* Future themes */ }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("All Lists") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                                        viewModel.pinSelectedCards()
                                        showDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete selected") },
                                    onClick = {
                                        viewModel.deleteSelectedCards()
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
                    items(state.cards, key = { it.id }) { item ->
                        CardItem(
                            card = item,
                            onCheckedChange = { isChecked ->
                                viewModel.updateCardSelection(item.id, isChecked)
                            },
                            onClick = {
                                context.startActivity(
                                    Intent(context, CardDetailActivity::class.java).apply {
                                        putExtra("CARD_TITLE", item.title)
                                        putExtra("CARD_ID", item.id)
                                    }
                                )
                            },
                            onEditClick = { editingCard = item },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCardDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, isPinned ->
                viewModel.addCard(title, isPinned)
                showAddDialog = false
            }
        )
    }

    editingCard?.let { card ->
        EditCardDialog(
            card = card,
            onDismiss = { editingCard = null },
            onSave = { title, isPinned ->
                viewModel.updateCard(card.id, title, isPinned)
                editingCard = null
            }
        )
    }
}

@Composable
fun DrawerContent(onRemindersClick: () -> Unit, onThemesClick: () -> Unit) {
    ModalDrawerSheet(modifier = Modifier.width(240.dp)) {
        Text("Menu", modifier = Modifier.padding(16.dp))
        NavigationDrawerItem(
            label = { Text("Reminders") }, selected = false, onClick = onRemindersClick
        )
        NavigationDrawerItem(
            label = { Text("Themes") }, selected = false, onClick = onThemesClick
        )
    }
}

// Update AddCardDialog to include pin checkbox
@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New List") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Title") },
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
                onClick = { onConfirm(title, isPinned) },
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

@Composable
fun EditCardDialog(
    card: Card,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(card.title) }
    var isPinned by remember { mutableStateOf(card.isPinned) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit List") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Title") },
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
                onClick = { onSave(title, isPinned) },
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

@Composable
fun CardItem(
    card: Card,
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
            checked = card.isSelected,
            onCheckedChange = { onCheckedChange(it) },
            modifier = Modifier.clickable { onCheckedChange(!card.isSelected) }
        )
        Text(
            text = card.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (card.isPinned) FontWeight.W900 else FontWeight.Normal
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