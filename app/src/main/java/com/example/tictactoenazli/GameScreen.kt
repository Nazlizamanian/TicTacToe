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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun GameScreen(navController: NavController, model: GameModel, gameId: String?) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    if (gameId != null && games.containsKey(gameId)) {
        Scaffold(
            topBar = { TopAppBar(title =  { Text("TicTacToe - $gameId") }) }
        ) { innerPadding ->
            Column(modifier = Modifier
                .padding(innerPadding)) {

                Text("Game state: ${games[gameId]!!.gameState}")

                Column( //Board
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = BabyPink)
                        .padding(15.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text("Game status")
                    Spacer(modifier = Modifier.padding(10.dp))
                    Row (
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ){
                        Box( modifier = Modifier
                            .padding(5.dp)
                            .size(100.dp)
                            .background(Color.White, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ){
                            Text(text= "Player 1: ${players[games[gameId]!!.player1Id]!!.name}",
                                style = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign= TextAlign.Center))
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Box( modifier = Modifier.padding(5.dp).size(100.dp)
                            .background(Color.White, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ){
                            Text(text= "Player 2: ${players[games[gameId]!!.player2Id]!!.name}",
                                style = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign= TextAlign.Center))
                        }

                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        items(9){ index ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(5.dp).size(100.dp).background(color = Color.White, RoundedCornerShape(13.dp))
                                    .border(1.dp, color = Color.White, RoundedCornerShape(13.dp))
                                    .clickable {  }
                            ){
                                Text(
                                    text = "Text",
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

                }

            }
        }
    } else {
        Log.e(
            "Error",
            "Error Game not found: $gameId"
        )
        navController.navigate("lobby")
    }
}