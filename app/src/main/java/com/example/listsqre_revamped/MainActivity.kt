package com.example.listsqre_revamped

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.listsqre_revamped.ui.theme.SimpleUITheme
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val _isDialogVisible = mutableStateOf(false)
    val cardList: SnapshotStateList<String> = mutableStateListOf()
    val isDialogVisible: State<Boolean> get() = _isDialogVisible
    fun showDialog() {
        _isDialogVisible.value = true
    }
    fun hideDialog() {
        _isDialogVisible.value = false
    }
    fun addCard() {
        cardList.add("Card ${cardList.size + 1}")
    }
    fun deleteCard(card: String) {
        cardList.remove(card)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleUITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(sharedViewModel: SharedViewModel = viewModel()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val drawerItems = listOf("Home", "Profile", "Settings")
    var selectedItem by remember { mutableStateOf(drawerItems[0]) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Listsqre_v2",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item) },
                        selected = item == selectedItem,
                        onClick = {
                            selectedItem = item
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedItem) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Icon")
                        }
                    }
                )
            },
            floatingActionButton = {
                MultipleFABs()
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                CardList(
                    cardList = sharedViewModel.cardList,
                    onDeleteCard = { sharedViewModel.deleteCard(it) }
                )
            }
            // Show the dialog when `showDialog` is true
            if (sharedViewModel.isDialogVisible.value) {
                ExampleDialog(onDismiss = { sharedViewModel.hideDialog() })
            }
        }
    }
}

@Composable
fun MultipleFABs(sharedViewModel: SharedViewModel = viewModel()) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        // modifier = Modifier.padding(bottom = 16.dp, end = 16.dp) // padding already defined
    ) {
        FloatingActionButton(
            onClick = {
                sharedViewModel.addCard()
            },
            containerColor = MaterialTheme.colorScheme.tertiary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
        FloatingActionButton(
            onClick = {
                sharedViewModel.showDialog()
            },
            containerColor = MaterialTheme.colorScheme.tertiary
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
fun CardList(
    cardList: List<String>,
    onDeleteCard: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cardList) { card ->
            CardView(cardTitle = card, onDelete = { onDeleteCard(card) })
        }
    }
}

@Composable
fun CardView(
    cardTitle: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = cardTitle,
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = { onDelete() }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Card")
            }
        }
    }
}

@Composable
fun ExampleDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() }, // Close dialog when clicking outside or pressing back
        title = { Text(text = "Dialog Title") },
        text = { Text(text = "This is a simple dialog triggered by the FAB.") },
        confirmButton = {
            TextButton(onClick = { /* TODO: do data processing */ }) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingUIPreview() {
    SimpleUITheme {
        AppContent()
    }
}
