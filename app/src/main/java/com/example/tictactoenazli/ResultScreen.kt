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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.tictactoenazli.ui.theme.BabyPink
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun ResultScreen(navController: NavController, model: GameModel, gameId: String?){
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    val game = gameId?.let { games[it] }

    if (game != null) {
        val player1Name = players[game.player1Id]?.name ?: "Player 1"
        val player2Name = players[game.player2Id]?.name ?: "Player 2"

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BabyPink)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display winner text (you can replace the "fixa" with the actual winner)
            Text(
                text = "Winner is ${ if (game.gameState == "player1_won") player1Name 
                else if (game.gameState == "player2_won") player2Name 
                else "No winner, its a draw!" }",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Trophy icon for winner ish
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Winner Trophy",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Yellow
                )
                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Loser Icon",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center){
                    Text("Player 1: $player1Name")
                    Text("Player 2: $player2Name")

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // klickbar box aka button to navigate back to lobby.
            Box(
                modifier = Modifier
                    .clickable {
                        navController.navigate(Screen.LobbyScreen.route)
                    }
                    .padding(10.dp)
                    .shadow(10.dp, RoundedCornerShape(13.dp))
                    .background(Color.White, RoundedCornerShape(13.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(13.dp))
                    .fillMaxWidth(),

                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Go to Lobby",
                    style = MaterialTheme.typography.displayMedium,
                )

            }
        }
    } else {
        Log.e("Error", "Game not found: $gameId")
        navController.navigate("lobby")
    }
}

