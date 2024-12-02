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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.tictactoenazli.ui.theme.BabyPink
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow




@Composable
fun GameScreen(navController: NavController, model: GameModel, gameId: String?) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()


    if (gameId != null && games.containsKey(gameId)) {
        val game = games[gameId]
        val gameState = game?.gameState

        val localPlayerId = model.localPlayerId.value

        val isLocalPlayerTurn = when (gameState) {
            "player1_turn" -> localPlayerId == game?.player1Id
            "player2_turn" -> localPlayerId == game?.player2Id
            else -> false
        }

        var isGameOver by remember { mutableStateOf(false) }

        LaunchedEffect(gameState) {
            if (gameState == "player1_won" || gameState == "player2_won" || gameState == "draw") {
                isGameOver = true
                delay(1500) // Delay
                if (gameId != null && games.containsKey(gameId)) {
                    navController.navigate("resultScreen/$gameId")
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = BabyPink)
                .padding(15.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (gameState == "player1_turn" || gameState == "player2_turn") {
                if (isLocalPlayerTurn) {
                    Text(
                        text = "Your Turn",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    val opponentName = if (localPlayerId == game?.player1Id) {
                        players[game?.player2Id]?.name
                    } else {
                        players[game?.player1Id]?.name
                    }
                    Text(
                        text = "${opponentName ?: "Opponents"}'s Turn",
                        style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize= 50.sp,
                            color = Color.Gray
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (isGameOver) {
                Text(
                    text = "Game Over",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.padding(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(100.dp)
                        .shadow(5.dp, RoundedCornerShape(13.dp))
                        .background(Color.White, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Player 1: ${players[games[gameId]!!.player1Id]!!.name} ",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(100.dp)
                        .shadow(5.dp, RoundedCornerShape(13.dp))
                        .background(Color.White, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Player 2: ${players[games[gameId]!!.player2Id]!!.name}",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(16.dp)
            ) {
                items(9) { index ->
                    val cellState = games[gameId]?.gameBoard?.get(index) ?: 0
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(5.dp)
                            .size(100.dp)
                            .shadow(5.dp, RoundedCornerShape(13.dp))
                            .background(color = Color.White, RoundedCornerShape(13.dp))
                            .border(1.dp, color = Color.White, RoundedCornerShape(13.dp))
                            .clickable {
                                val game = games[gameId] ?: return@clickable
                                val playerId = model.localPlayerId.value

                                if (game.gameBoard[index] == 0 &&
                                    ((game.gameState == "player1_turn" && playerId == game.player1Id) ||
                                            (game.gameState == "player2_turn" && playerId == game.player2Id))
                                ) {
                                    model.checkGameState(gameId, index)
                                }
                            }
                    ) {
                        Text(
                            text = when (cellState) {
                                1 -> "X"
                                2 -> "O"
                                else -> ""
                            },
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 83.sp,
                                color = BabyPink
                            ),
                            modifier = Modifier.fillMaxSize(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

        }//column end

    }

}