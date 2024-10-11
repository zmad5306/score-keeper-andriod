package dev.zachmaddox.scorekeeper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
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
    fun MyAppBar(onNewGame: (targetScore: Int) -> Unit) {
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

                                try {
                                    val targetScore = value.toInt();
                                    onNewGame(targetScore.or(0))
                                } catch (e: NumberFormatException) {
                                    // Do nothing, invalid input
                                }
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun PlayerCard(player: Player, whenScoreEntered: (player: Player, points: Int) -> Unit, whenPlayerRemoved: (player: Player) -> Unit) {
        val inputPlayerScore = remember { mutableStateOf(false) }
        val haptics = LocalHapticFeedback.current
        val showContextMenu = remember { mutableStateOf(false) }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1f)
                .combinedClickable(
                    onClick = { inputPlayerScore.value = !inputPlayerScore.value },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showContextMenu.value = true
                    }
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Filled.Person, contentDescription = "Player Icon")
                Text(player.name, modifier = Modifier.padding(16.dp))
                if (inputPlayerScore.value) {
                    val handScore = remember { mutableStateOf("") }
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    TextField(handScore.value, onValueChange = { newHandScore ->
                            handScore.value = newHandScore
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                inputPlayerScore.value = false

                                try {
                                    val points = handScore.value.toInt();
                                    whenScoreEntered(player, points.or(0))
                                } catch (e: NumberFormatException) {
                                    // Do nothing, invalid input
                                }

                                handScore.value = ""
                            }
                        ),
                        label = { Text(text = "Score") },
                        modifier = Modifier.focusRequester(focusRequester),
                        singleLine = true,
                    )
                }
                else {
                    Badge(
                        modifier = Modifier.padding(16.dp),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary,
                        content = {
                            Text(player.score.toString())
                        })
                }

            }
            DropdownMenu(
                expanded = showContextMenu.value,
                onDismissRequest = { showContextMenu.value = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Remove") },
                    onClick = {
                        whenPlayerRemoved(player)
                        showContextMenu.value = false
                    }
                )
            }
        }
    }

    @Composable
    fun ScoreKeeperApp() {
        val model = PlayerViewModel(this)
        Scaffold(
            topBar = { MyAppBar(onNewGame = { targetScore ->
                model.resetScores()
                model.setPlayingTo(targetScore)
            }) },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = { AddPlayerButton(whenConfirmed = {playerName ->
                run {
                    Log.d("Main Activity", "Adding player $playerName")
                    model.addPlayer(playerName)
                }
            }) }
        ) { paddingValues ->
             Column(modifier = Modifier.padding(paddingValues)) {
                 if (model.playingTo.intValue > 0) {
                     Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                         Text("Playing to: ${model.playingTo.intValue}")
                     }
                 }

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
                     items(model.players.size) { index ->
                         PlayerCard(model.players[index], whenScoreEntered = { playerName, points ->
                             run {
                                 Log.d("Main Activity", "Got score for $playerName: $points")
                                 model.incrementScore(model.players[index], points)
                             }
                         }, whenPlayerRemoved = {player ->
                             run {
                                 Log.d("Main Activity", "Player ${player.name} removed")
                                 model.removePlayer(player);
                             }
                         })
                     }
                 }
             }
        }
    }

}
