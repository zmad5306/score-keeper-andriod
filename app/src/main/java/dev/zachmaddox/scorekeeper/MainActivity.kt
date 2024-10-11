package dev.zachmaddox.scorekeeper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScoreKeeperApp()
        }
    }

    @Composable
    fun InputDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: (String) -> Unit,
        dialogTitle: String,
        dialogText: String,
        icon: ImageVector,
        keyboardType: KeyboardType = KeyboardType.Text,
    ) {
        var text by remember { mutableStateOf("") }

        AlertDialog(
            icon = {
                Icon(icon, contentDescription = "Icon")
            },
            title = {
                Text(text = dialogTitle)
            },
            text = {
                TextField(text, onValueChange = { newText ->
                        text = newText
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    label = { Text(text = dialogText) }
                )
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation(text)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    @Composable
    fun AddPlayerButton(whenConfirmed: (name: String) -> Unit) {
        val openNewPlayerDialog = remember { mutableStateOf(false) }

        FloatingActionButton(
            onClick = { openNewPlayerDialog.value = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Filled.Add, "Add player")
        }

        when {
            openNewPlayerDialog.value -> {
                InputDialog(
                    onDismissRequest = { openNewPlayerDialog.value = false },
                    onConfirmation = { value ->
                        openNewPlayerDialog.value = false
                        whenConfirmed(value)
                    },
                    dialogTitle = "Add a player?",
                    dialogText = "Name",
                    icon = Icons.Default.Person
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MyAppBar() {
        val openNewGameDialog = remember { mutableStateOf(false) }

        TopAppBar(
            title = {
                Text("ScoreKEEPER")
            },
//            navigationIcon = {
//                IconButton(onClick = { /* Handle navigation icon click */ }) {
//                    Icon(Icons.Default.Menu, contentDescription = "Menu")
//                }
//            },
            actions = {
                FilledTonalButton(onClick = { openNewGameDialog.value = true }) {
                    Text("New Game")
                }

                when {
                    openNewGameDialog.value -> {
                        InputDialog(
                            onDismissRequest = { openNewGameDialog.value = false },
                            onConfirmation = { value ->
                                openNewGameDialog.value = false
                                Log.d("MainActivity", "Got winning score of: $value")
                                // TODO set winning score
                            },
                            dialogTitle = "Start a new game?",
                            dialogText = "How many points to win?",
                            icon = Icons.Default.PlayArrow,
                            keyboardType = KeyboardType.Number,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }

    @Composable
    fun PlayerCard(name: String) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1f),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(name, modifier = Modifier.padding(16.dp))
                Text("100", modifier = Modifier.padding(16.dp))
            }

        }
    }

    @Composable
    fun ScoreKeeperApp() {
        val players = remember { mutableListOf("Zach", "Allison", "Hannah", "Haddie", "Aidan", "Hazel") }
        Scaffold(
            topBar = { MyAppBar() },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = { AddPlayerButton(whenConfirmed = {playerName ->
                run {
                    Log.d("Main Activity", "Adding player $playerName")
                    players.add(playerName)
                }
            }) }
        ) { paddingValues ->
             Column(modifier = Modifier.padding(paddingValues)) {
                 LazyVerticalGrid(
                     columns = GridCells.Fixed(2),
                     verticalArrangement = Arrangement.spacedBy(16.dp),
                     horizontalArrangement = Arrangement.spacedBy(16.dp),
                     modifier = Modifier.padding(
                         top = 16.dp,
                         start = 16.dp,
                         end = 16.dp,
                         bottom = 16.dp,
                     )
                 ) {
                     items(players.size) { index ->
                         PlayerCard(players[index])
                     }
                 }
             }
        }
    }

}
