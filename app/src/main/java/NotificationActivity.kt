package com.example.listsqre_revamped

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.listsqre_revamped.ui.CardAppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CardAppTheme {
                val app = LocalContext.current.applicationContext as MyApplication
                val viewModel = remember {
                    NotificationViewModel(app.database.notificationDao())
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotificationAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationAppScreen(viewModel: NotificationViewModel = viewModel()) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoadingForNoti.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTimePicker = true },
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
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Text(
                    text = "Please wait...",
                    modifier = Modifier.align(Alignment.Center)
                )
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
                    // verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications.cards, key = { it.id }) { item ->
                        NotificationCard(
                            notification = item,
                            onCancel = {
                                cancelNotification(context, item.uniqueId, item.description)
                                viewModel.cancelNotification(item)
                            },
                            onClick = { /* do nothing as of now */ },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute, desc ->
                val uid = scheduleNotification(context, hour, minute, desc)
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val notification = NotificationEntity(
                    uniqueId = uid,
                    description = desc,
                    notificationTime = calendar.timeInMillis
                )
                viewModel.insert(notification)
            }
        )
    }
}

@Composable
fun TextFieldTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (Int, Int) -> Unit
) {
    var hourText by remember { mutableStateOf(hour.toString().padStart(2, '0')) }
    var minuteText by remember { mutableStateOf(minute.toString().padStart(2, '0')) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
    ) {
        OutlinedTextField(
            value = hourText,
            onValueChange = {
                hourText = it.filter { c -> c.isDigit() }.take(2)
                hourText.toIntOrNull()?.let { h ->
                    if (h in 0..23) onTimeChange(h, minute)
                }
            },
            label = { Text("Hour (24hr)") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text(":", modifier = Modifier.padding(bottom = 0.dp))

        OutlinedTextField(
            value = minuteText,
            onValueChange = {
                minuteText = it.filter { c -> c.isDigit() }.take(2)
                minuteText.toIntOrNull()?.let { m ->
                    if (m in 0..59) onTimeChange(hour, m)
                }
            },
            label = { Text("Minute (24hr)") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int, description: String) -> Unit
) {
    var hour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(hour, minute, description)
                    onDismiss()
                },
                enabled = description.isNotBlank()
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Set Reminder") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextFieldTimePicker(hour, minute) { newHour, newMinute ->
                    hour = newHour
                    minute = newMinute
                }
            }
        }
    )
}

@Composable
fun NotificationCard(
    notification: NotificationEntity,
    onCancel: (NotificationEntity) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedTime = SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()).format(Date(notification.notificationTime)
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = notification.description + "\nScheduled later at ≈$formattedTime",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { onClick() }
            )
            IconButton(
                onClick = { onCancel(notification) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Cancel"
                )
            }
        }
    }
}