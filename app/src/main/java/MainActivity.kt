package com.example.listsqre_revamped

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.listsqre_revamped.ui.ComposeAppTheme

class MainActivity : ComponentActivity() {
    private val database by lazy { CardDatabase.getDatabase(this) }
    private val cardDao by lazy { database.cardDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        onNotificationClick = { openNotificationActivity() }
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

    private fun openNotificationActivity() {
        startActivity(Intent(this, NotificationActivity::class.java))
    }
}

@Composable
fun CardManager(
    cards: List<Pair<String, Boolean>>,
    onCardsChanged: (List<Pair<String, Boolean>>) -> Unit,
    onNotificationClick: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(onNotificationClick)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
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
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onCardsChanged(cards.filterNot { it.second }) },
                enabled = cards.any { it.second }
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Card")
            }

            Button(onClick = onNotificationClick) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
            }

            Button(onClick = { showCreateDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(onNotificationClick: () -> Unit) {
    TopAppBar(
        title = { Text("Listsqre_v2", fontSize = 20.sp) },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications"
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
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(text) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
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
    onCheckedChange: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(16.dp)
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
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            )
        }
    }
}
