package com.example.tictactoenazli

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.tictactoenazli.ui.theme.BabyPink
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
        LazyColumn(modifier = Modifier.padding(innerPadding). background(BabyPink)) {
            items(players.entries.toList()) { (documentId, player) ->
                if (documentId != model.localPlayerId.value) { // Don't show yourself'

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                            .shadow(10.dp, RoundedCornerShape(13.dp))
                            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp) // Inner padding for content
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Player Icon",
                                modifier = Modifier.size(40.dp),
                                tint = BabyPink
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                // Headline Text
                                Text(
                                    text = "Player Name: ${player.name}",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                // Supporting Text
                                Text(
                                    text = "Status: ...",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                )
                            }

                            // Trailing Content
                            var hasGame = false
                            games.forEach { (gameId, game) ->
                                if (game.player1Id == model.localPlayerId.value && game.gameState == "invite") {
                                    Text(
                                        text = "Waiting for accept...",
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    )
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
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = BabyPink,
                                            shape = RoundedCornerShape(13.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 12.dp)
                                        .clickable {
                                            model.db.collection("games")
                                                .add(
                                                    Game(
                                                        gameState = "invite",
                                                        player1Id = model.localPlayerId.value!!,
                                                        player2Id = documentId
                                                    )
                                                )
                                                .addOnSuccessListener { documentRef ->
                                                    // TODO: Navigate?
                                                }
                                        }
                                ) {
                                    Text(
                                        text = "Challenge",
                                        color = Color.White,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}