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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.tictactoenazli.ui.theme.BabyPink
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, model: GameModel) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    var showDeclineNotification by remember { mutableStateOf(false) }
    var declinedPlayerName by remember { mutableStateOf("") }

    LaunchedEffect(games) { //courotine körs varje gång game uppdateras, nav -> gameScreen
        games.forEach { (gameId, game) ->
            if ((game.player1Id == model.localPlayerId.value || game.player2Id == model.localPlayerId.value)
                && (game.gameState == "player1_turn" || game.gameState == "player2_turn")
            ) {
                navController.navigate("game/${gameId}")
            }
        }
    }

    //Ifall spelare inte kan hittas (fallback)
    var playerName = "Unknown?"
    players[model.localPlayerId.value]?.let {
        playerName = it.name
    }

    Scaffold( //Struktur av sidan
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TicTacToe - $playerName",
                        style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Thin,
                            textAlign = TextAlign.Center,
                            fontSize = 40.sp,
                            fontFamily = FontFamily.Cursive
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(BabyPink)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn( //Har vertical scroll snyggare om det är massor av spelare.
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(BabyPink)
            ) {
                items(players.entries.toList()) { (documentId, player) ->
                    if (documentId != model.localPlayerId.value) { //Iterera över all players.
                        Box( //Varje spelare ruta med info & ikon etc.
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

                                    Spacer(modifier = Modifier.height(8.dp))

                                    var hasGame = false //Pågående inbjudan t alla spelare F ingen fått inbjudan än.
                                    games.forEach { (gameId, game) ->
                                        //Lokalisera spelare t vår localPlayer o om andra spelare behandlats i iterationen
                                        if (game.player1Id == model.localPlayerId.value && game.player2Id == documentId && game.gameState == "invite") {
                                            Text(
                                                text = "Waiting for ${player.name} to accept...",
                                                style = androidx.compose.ui.text.TextStyle(
                                                    fontStyle = FontStyle.Italic,
                                                    color = Color.Gray
                                                )
                                            )
                                            hasGame = true

                                            //Spelare 2 skickat inbjudan t spelare1
                                        } else if (game.player2Id == model.localPlayerId.value && game.player1Id == documentId && game.gameState == "invite") {
                                            Row {
                                                Button(
                                                    onClick = { //Om accept upd gameState o -> game
                                                        model.db.collection("games").document(gameId)
                                                            .update("gameState", "player1_turn")
                                                            .addOnSuccessListener {
                                                                navController.navigate("game/${gameId}")
                                                            }
                                                            .addOnFailureListener {
                                                                Log.e("LobbyError", "Error updating game: $gameId")
                                                            }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                                                ) {
                                                    Text("Accept", color = Color.White)
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Button( //Decline Button upd gameState i db
                                                    onClick = {
                                                        model.db.collection("games").document(gameId)
                                                            .update("gameState", "declined")
                                                            .addOnSuccessListener {
                                                                Log.d("LobbyInfo", "Game invite declined")
                                                            }
                                                            .addOnFailureListener {
                                                                Log.e("LobbyError", "Error declining game: $gameId")
                                                            }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                                ) {
                                                    Text("Decline", color = Color.White)
                                                }
                                            }
                                            hasGame = true
                                        } else if (game.player1Id == model.localPlayerId.value && game.player2Id == documentId && game.gameState == "declined") {
                                            model.db.collection("games").document(gameId).delete()
                                            declinedPlayerName = player.name
                                            showDeclineNotification = true
                                            hasGame = true
                                        }
                                    }
                                    if (!hasGame) {
                                        Button(
                                            onClick = {
                                                model.db.collection("games")
                                                    .add(
                                                        Game(
                                                            gameState = "invite",
                                                            player1Id = model.localPlayerId.value!!,
                                                            player2Id = documentId
                                                        )
                                                    )
                                                    .addOnSuccessListener { documentRef ->
                                                        Log.d("LobbyInfo", "Invite sent to ${player.name}")
                                                    }
                                                    .addOnFailureListener {
                                                        Log.e("LobbyError", "Error sending invite to ${player.name}")
                                                    }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BabyPink)
                                        ) {
                                            Text("Challenge")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //Pop up fösnter för spelarens inbjudan som blev nekad :(.
            if (showDeclineNotification) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(enabled = false ){},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Player $declinedPlayerName declined your invite",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(colors = ButtonDefaults.buttonColors(containerColor = BabyPink),
                                onClick = { showDeclineNotification = false }) {
                                Text("Close")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}
