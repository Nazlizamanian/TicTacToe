package com.example.tictactoenazli

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.tictactoenazli.ui.theme.BabyPink
import kotlinx.coroutines.flow.asStateFlow
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, model: GameModel) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    var challengedPlayerId by remember { mutableStateOf<String?>(null) }
    val localPlayerId = model.localPlayerId.value!!

    // Detect if the local player is part of a game with "player1_turn" state
    LaunchedEffect(games) {
        games.forEach { (gameId, game) ->
            if ((game.player1Id == localPlayerId || game.player2Id == localPlayerId) &&
                game.gameState == "player1_turn"
            ) {
                navController.navigate("game/${gameId}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text ="TicTacToe - ${players[localPlayerId]?.name ?: "Unknown"}",
                        style = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Thin,
                            textAlign = TextAlign.Center,
                            fontSize = 40.sp,
                            fontFamily = FontFamily.Cursive
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        },
        modifier = Modifier.fillMaxSize().background(BabyPink)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(BabyPink)
        ) {
            items(players.entries.toList()) { (playerId, player) ->
                if (playerId != localPlayerId) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                            .shadow(10.dp, RoundedCornerShape(13.dp))
                            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Player Icon",
                                modifier = Modifier.size(60.dp),
                                tint = BabyPink
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Player Name: ${player.name}",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Status: ...",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                )
                            }
                           // Spacer(modifier = Modifier.padding(4.dp))

                            if (challengedPlayerId == playerId) {
                                Text(
                                    text = "Waiting for accept...",
                                    style = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                )
                            } else if (games.any { it.value.player1Id == playerId && it.value.player2Id == localPlayerId && it.value.gameState == "invite" }) {
                                Button(onClick = {
                                    val game = games.entries.find {
                                        it.value.player1Id == playerId &&
                                                it.value.player2Id == localPlayerId &&
                                                it.value.gameState == "invite"
                                    }
                                    game?.let { (gameId, _) ->
                                        model.db.collection("games").document(gameId)
                                            .update("gameState", "player1_turn")
                                            .addOnSuccessListener {
                                                navController.navigate("game/${gameId}")
                                            }
                                            .addOnFailureListener {
                                                Log.e("Error", "Error accepting game invite: $gameId")
                                            }
                                    }
                                }) {
                                    Text("Accept invite")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        challengedPlayerId = playerId
                                        model.db.collection("games")
                                            .add(
                                                Game(
                                                    gameState = "invite",
                                                    player1Id = localPlayerId,
                                                    player2Id = playerId
                                                )
                                            )
                                            .addOnSuccessListener {
                                                Log.d("Game", "Challenge sent to $playerId")
                                            }
                                            .addOnFailureListener {
                                                Log.e("Error", "Error sending challenge to $playerId")
                                            }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BabyPink
                                    )
                                ) {
                                    Text(
                                        text = "Challenge",
                                        color = Color.White,
                                        style = androidx.compose.ui.text.TextStyle(
                                            fontSize = 16.sp,
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
