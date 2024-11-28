package com.example.tictactoenazli

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.asStateFlow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, model: GameModel) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()


    var showPopup by remember { mutableStateOf(false) }
    var gameToAccept by remember { mutableStateOf<Pair<String, Game>?>(null) }

    LaunchedEffect(games) {
        games.forEach { (gameId, game) ->
            // TODO: Popup with accept invite?

            if ((game.player1Id == model.localPlayerId.value || game.player2Id == model.localPlayerId.value) && game.gameState == "player1_turn") {
                navController.navigate("game/${gameId}")
                //gameToAccept = gameId to game
               // showPopup = true
            }
        }
    }
    if(showPopup){
        gameToAccept?.let { (gameId, game)->
            Dialog(onDismissRequest = {showPopup = false}) {
                Surface (
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(16.dp)
                ){
                    Column (modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Game Invite",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "${players[game.player1Id]?.name ?: "Unknown"} has invited you to a game.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ){
                            Button(
                                onClick = {
                                    showPopup = false
                                    model.db.collection("games").document(gameId)
                                        .update("gameState", "player1_turn")
                                        .addOnSuccessListener {
                                            navController.navigate("game/${gameId}")
                                        }
                                        .addOnFailureListener {
                                            Log.e("Error", "Error accepting game invite: $gameId")
                                        }
                                }
                            ) {
                                Text("Accept")
                            }
                            Button(
                                onClick = {
                                    showPopup = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Decline")
                            }

                        }

                    }

                }
            }

        }

    }

    var playerName = "Unknown?"
    players[model.localPlayerId.value]?.let { playerName = it.name }

    Scaffold(
        topBar = { TopAppBar(title =  { Text("TicTacToe - $playerName") }) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(players.entries.toList()) { (documentId, player) ->
                if (documentId != model.localPlayerId.value) { // Don't show yourself'
                    ListItem(
                        headlineText = {
                            Text("Player Name: ${player.name}") },
                        supportingText = {
                            Text("Status: ...") },
                        trailingContent = {
                            var hasGame = false
                            games.forEach { (gameId, game) ->
                                if (game.player1Id == model.localPlayerId.value && game.gameState == "invite") {
                                    Text("Waiting for accept...")
                                    hasGame = true
                                } else if (game.player2Id == model.localPlayerId.value && game.gameState == "invite") {
                                    Button(onClick = {
                                        model.db.collection("games").document(gameId)
                                            .update("gameState", "player1_turn")
                                            .addOnSuccessListener {
                                                navController.navigate("game/${gameId}")
                                            }
                                            .addOnFailureListener {
                                                Log.e("Error", "Error updating game: $gameId")
                                            }
                                    }) {
                                        Text("Accept invite")
                                    }
                                    hasGame = true
                                }
                            }
                            if (!hasGame) {
                                Button(onClick = {
                                    model.db.collection("games")
                                        .add(Game(gameState = "invite", player1Id = model.localPlayerId.value!!, player2Id = documentId))
                                        .addOnSuccessListener { documentRef ->
                                            // TODO: Navigate?
                                        }
                                }) {
                                    Text("Challenge")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}