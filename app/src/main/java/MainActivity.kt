package com.example.listsqre_revamped

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.listsqre_revamped.ui.ComposeAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val database by lazy { CardDatabase.getDatabase(this) }
    private val cardDao by lazy { database.cardDao() }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionReq(this)

        setContent {
            val cardsState = remember { mutableStateListOf<Pair<String, Boolean>>() }
            val scope = rememberCoroutineScope()

            // Load cards asynchronously
            LaunchedEffect(Unit) {
                loadCards(cardsState)
            }

            ComposeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CardManager(
                        cards = cardsState,
                        onCardsChanged = { newCards ->
                            cardsState.clear()
                            cardsState.addAll(newCards)
                            scope.launch { saveCards(newCards) }  // Save updated cards to DB
                        },
                        onSettingsClick = {
                            openSettingsActivity()
                        },
                        this
                    )
                }
            }
        }
    }

    private suspend fun loadCards(cardsState: MutableList<Pair<String, Boolean>>) {
        withContext(Dispatchers.IO) {
            val savedCards = cardDao.getAllCards().map { it.name to it.isChecked }
            withContext(Dispatchers.Main) {
                cardsState.clear()
                cardsState.addAll(savedCards)
            }
        }
    }

    private suspend fun saveCards(cards: List<Pair<String, Boolean>>) {
        withContext(Dispatchers.IO) {
            cardDao.deleteAllCards()
            cardDao.insertCards(cards.map { CardItem(it.first, it.second) })
        }
    }

    private fun openSettingsActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun permissionReq(context: Context) {
        val permission = "android.permission.POST_NOTIFICATIONS"
        val permissionState = ContextCompat.checkSelfPermission(context, permission)
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(context, "Enable notifications", Toast.LENGTH_SHORT).show()
                } else { /* do nothing */ }
            }
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission)
        } else { /* do nothing */ }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CardManager(
    cards: List<Pair<String, Boolean>>,
    onCardsChanged: (List<Pair<String, Boolean>>) -> Unit,
    onSettingsClick: () -> Unit,
    context: Context
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var selectedTime by rememberSaveable { mutableStateOf("Select Time") }

    // Function to handle menu item clicks
    fun onMenuItemClick(action: String) {
        when (action) {
            "shift" -> {
                val (pinned, unpinned) = cards.partition { it.second }
                val updatedCards = pinned.map { it.copy(second = false) } + unpinned
                onCardsChanged(updatedCards) // Move pinned items to top and uncheck them
            }
            "notify" -> showTimePicker = true  // Show time picker for notifications
            "delete" -> onCardsChanged(cards.filterNot { it.second }) // Delete checked cards
        }
    }

    if (showCreateDialog) {
        CardDialog(
            initialText = "",
            title = "Create New Item",
            onDismiss = { showCreateDialog = false },
            onSave = { cardName ->
                if (cardName.isNotBlank()) {
                    onCardsChanged(cards + Pair(cardName, false))
                }
                showCreateDialog = false
            }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = { hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                showTimePicker = false
            },
            context
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(onSettingsClick, ::onMenuItemClick) // Pass menu click handler

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards) { card ->
                CardLayout(
                    cardName = card.first,
                    isChecked = card.second,
                    onCheckedChange = { isChecked ->
                        onCardsChanged(cards.map {
                            if (it.first == card.first) it.copy(second = isChecked) else it
                        })
                    },
                    onEdit = { newName ->
                        onCardsChanged(cards.map {
                            if (it.first == card.first) Pair(newName, it.second) else it
                        })
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { showCreateDialog = true }
            ) {
                Icon(imageVector = Icons.Default.Add,
                    contentDescription = "Add Card")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(onSettingsClick: () -> Unit, onMenuItemClick: (String) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Listsqre", fontSize = 20.sp) },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu"
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Move up") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Settings"
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onMenuItemClick("shift")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Notify") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Settings"
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onMenuItemClick("notify")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Settings"
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onMenuItemClick("delete")
                    }
                )
            }
        }
    )
}

@Composable
fun CardDialog(
    initialText: String,
    title: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf(initialText) }
    val keyboardController = LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp, max = 120.dp), // No vertical scroll
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(text)
                    keyboardController?.hide() // Hide keyboard after saving
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    keyboardController?.hide() // Hide keyboard after canceling
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Cancel") }
        },
        modifier = Modifier.wrapContentSize(Alignment.BottomCenter)
    )
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    context: Context
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                    clockDialSelectedContentColor = MaterialTheme.colorScheme.primary,
                    selectorColor = MaterialTheme.colorScheme.onPrimary // Change hand color here
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                    scheduleNotification(context, timePickerState.hour, timePickerState.minute)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Cancel") }
        }
    )
}

@Composable
fun CardLayout(
    cardName: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: (String) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }

    if (showEditDialog) {
        CardDialog(
            initialText = cardName,
            title = "Edit Item",
            onDismiss = { showEditDialog = false },
            onSave = { newName ->
                if (newName.isNotBlank()) {
                    onEdit(newName)
                }
                showEditDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = cardName,
                    fontSize = 18.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                    // modifier = Modifier.animateContentSize()
                )
            }
            IconButton(onClick = { showEditDialog = true }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    }
}
