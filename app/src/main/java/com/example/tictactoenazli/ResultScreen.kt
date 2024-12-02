package com.example.tictactoenazli

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.tictactoenazli.ui.theme.BabyPink
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun ResultScreen(navController: NavController, model: GameModel, gameId: String?) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    val game = gameId?.let { games[it] }

    if (game != null) {
        val player1Name = players[game.player1Id]?.name ?: "Player 1"
        val player2Name = players[game.player2Id]?.name ?: "Player 2"

        val player1Score = players[game.player1Id]?.score?: 0
        val player2Score = players[game.player2Id]?.score?:0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BabyPink)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display winner text
            Text(
                text = "Winner is ${if (game.gameState == "player1_won") player1Name
                else if (game.gameState == "player2_won") player2Name
                else "no winner, it's a draw!" }",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
            )

            // Text-based Emojis and player names
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Player 1 Emoji
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when {
                                game.gameState == "player1_won" -> "👑"
                                game.gameState == "draw" -> "😞"
                                else -> "😞" //player 1 förlorat
                        },
                        style = TextStyle(fontSize = 64.sp), //styling för emoji
                    )
                    Text(
                        text = "Player 1: $player1Name\n Score: $player1Score",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }

                // Player 2 Emoji
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when {
                            game.gameState == "player2_won" -> "👑"
                            game.gameState == "draw" -> "😞"
                            else -> "😞" //player 2 förlorat
                        },
                        style = TextStyle(fontSize = 64.sp),

                    )
                    Text(
                        text = "Player 2: $player2Name\n Score: $player2Score",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Go to Lobby Button
            Box(
                modifier = Modifier
                    .clickable { navController.navigate(Screen.LobbyScreen.route) }
                    .padding(12.dp)
                    .shadow(10.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Go to Lobby",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    } else {
        Log.e("Error", "Game not found: $gameId")
        navController.navigate("lobby")
    }
}
