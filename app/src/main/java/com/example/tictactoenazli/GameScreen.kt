package com.example.tictactoenazli

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.tictactoenazli.ui.theme.BabyPink
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.Text



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController) {
    val db = Firebase.firestore //initisera ref t databasen, används för o fetcha
    val playersList = remember { mutableStateOf<List<Player>>(emptyList()) } //håller en lista of player objs
    //
    // Fetch players data from Firestore
    LaunchedEffect(true) {
        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                value?.let {
                    playersList.value = it.toObjects(Player::class.java) //konvertera snapshot t lista av player obj

                }
            }
    }

   // val players by playersList.collectAsState()

    // Board State
    val boardState = remember { mutableStateOf(List(9) { "" }) }
    val currentPlayer = remember { mutableStateOf("X") } //hmmm index1?
    val gameStatus = remember { mutableStateOf("Game In Progress") }
    val gameCount = remember { mutableStateOf(1) }

    val shouldResetBoard = remember { mutableStateOf(false) }

    fun resetBoard() {
        boardState.value = List(9){ ""}
    }

    fun updateScore(winner: String) {

    }



    // Handle cell click
    fun onCellClick(index: Int) {
        if (boardState.value[index] == "" && gameStatus.value == "Game In Progress") {
            val newBoardState = boardState.value.toMutableList()
            newBoardState[index] = currentPlayer.value
            boardState.value = newBoardState

            if (checkWin(newBoardState)) {
                val winningPlayer = if (currentPlayer.value == "X") {
                    playersList.value.getOrNull(0)?.playerName ?: "Player 1"
                } else {
                    playersList.value.getOrNull(1)?.playerName ?: "Player 2"
                }
                gameStatus.value = "$winningPlayer Wins!"

                // Award a point to the winning player
                updateScore(currentPlayer.value)

                if (gameCount.value >= 3) {
                    gameStatus.value = "$winningPlayer Wins!" //fixa detta
                    gameStatus.value = "Match Over!"
                } else {
                    gameCount.value++
                    shouldResetBoard.value = true
                }
            } else if (newBoardState.all { it.isNotEmpty() }) {
                gameStatus.value = "It's a Draw!"

                if (gameCount.value >= 3) {
                    gameStatus.value = "Match Over!"
                } else {
                    gameCount.value++
                    shouldResetBoard.value = true
                }
            } else {
                // Switch player
                currentPlayer.value = if (currentPlayer.value == "X") "O" else "X"
            }
        }
    }
    //Effekt i mellan ronderna .7s mellan varje rond innan den rensar bordet.
    LaunchedEffect(shouldResetBoard.value) {
        if (shouldResetBoard.value) {
            delay(700L)
            resetBoard()
            gameStatus.value = "Game In Progress"
            shouldResetBoard.value = false
        }
    }



    //Game board
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = BabyPink)
            .padding(16.dp)
    ) {
        Text(text = gameStatus.value, style = typography.titleLarge)
        Spacer(modifier = Modifier.padding(10.dp))

        if(playersList.value.isNotEmpty()){
            val currentPlayerName = if(currentPlayer.value == "X"){
                playersList.value.getOrNull(0)?.playerName ?: "Player 1"
            }
            else{
                playersList.value.getOrNull(1)?.playerName ?: "Player 2"
            }
            Text( text = "${currentPlayerName}'s Turn",  style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
        }
        Text( text = "Round ${gameCount.value}/3",  style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))

        Spacer(modifier = Modifier.padding(10.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            if (playersList.value.isNotEmpty()) {
                Box(modifier = Modifier
                    .padding(5.dp)
                    .size(100.dp)
                    .background( color = Color.White, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ){
                    val player = playersList.value.getOrNull(0)
                    Text(
                        text = "${player?.playerName ?: "Loading..."}\n${player?.score ?: 0}",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = BabyPink)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Box(modifier = Modifier
                    .padding(5.dp)
                    .size(100.dp)
                    .background(color = Color.White, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ){
                    val player = playersList.value.getOrNull(1)
                    Text(
                        text = "${player?.playerName ?: "Loading..."}\n${player?.score ?: 1}",
                        style = TextStyle(fontSize = 24.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = BabyPink)
                    )

                }

            }
        }


        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(16.dp)
        ) {
            items(9) { index ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(5.dp)
                        .size(100.dp)
                        .background(color = Color.White, RoundedCornerShape(13.dp))
                        .border(1.dp, color = Color.White, RoundedCornerShape(13.dp))
                        .clickable { onCellClick(index) }
                ) {
                    Text(
                        text = boardState.value[index],
                        style = TextStyle(fontSize= 83.sp, color = BabyPink),
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


//GAme logic
fun checkWin(board: List<String>): Boolean {
    val winPatterns = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),  // Rows
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),  // Columns
        listOf(0, 4, 8), listOf(2, 4, 6)  // Diagonals
    )


    for (pattern in winPatterns) {
        val (a, b, c) = pattern
        if (board[a] == board[b] && board[b] == board[c] && board[a].isNotEmpty()) {
            return true
        }
    }
    return false
}


