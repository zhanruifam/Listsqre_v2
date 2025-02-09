package com.example.listsqre_revamped

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.listsqre_revamped.ui.ComposeAppTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    settingsCardManager() // temporary layout filler
                }
            }
        }
    }
}

@Composable
fun settingsCardManager() {
    Column(modifier = Modifier.fillMaxSize()) {
        settingsAppHeader()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun settingsAppHeader() {
    TopAppBar(
        title = { Text("Settings", fontSize = 20.sp) },
    )
}