package com.example.listsqre_revamped

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.listsqre_revamped.ui.ComposeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CardManager { openNotificationActivity() }
                }
            }
        }
    }

    private fun openNotificationActivity() {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun CardManager(onNotificationClick: () -> Unit) {
    var cards by remember { mutableStateOf(mutableListOf<Pair<String, Boolean>>()) }
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        EditCardDialog(
            initialText = "",
            title = "Create New Item",
            onDismiss = { showCreateDialog = false },
            onSave = { cardName ->
                if (cardName.isNotBlank()) {
                    cards = (cards + Pair(cardName, false)).toMutableList()
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
                            cards = cards.map {
                                if (it.first == card.first) it.copy(second = isChecked) else it
                            }.toMutableList()
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
                onClick = { cards = cards.filterNot { it.second }.toMutableList() },
                enabled = cards.any { it.second },
                /* colors = ButtonDefaults.buttonColors(containerColor = if (cards.any { it.second }) MaterialTheme.colorScheme.secondary else Color.Gray) */
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Card",
                    tint = if (cards.any { it.second }) MaterialTheme.colorScheme.onPrimary else Color.LightGray
                )
            }

            Button(
                onClick = { showCreateDialog = true } /* onNotificationClick */
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Button(
                onClick = { showCreateDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Card",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(onNotificationClick: () -> Unit) {
    TopAppBar(
        title = {
            Text("Listsqre_v2", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp)
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun EditCardDialog(
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
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Cancel")
            }
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
            .padding(horizontal = 8.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = cardName,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            )
        }
    }
}

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(
                        text = "Notification Screen",
                        modifier = Modifier.fillMaxSize(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
