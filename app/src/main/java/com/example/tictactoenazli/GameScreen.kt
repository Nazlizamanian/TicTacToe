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
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController) {
    val db = Firebase.firestore
    val playersList = remember { mutableStateOf<List<Player>>(emptyList()) }

    // Fetch players data from Firestore
    LaunchedEffect(true) {
        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                value?.let {
                    playersList.value = it.toObjects(Player::class.java)
                    println("Players: ${playersList.value}")
                }
            }
    }

    // Board State
    val boardState = remember { mutableStateOf(List(9) { "" }) }
    val currentPlayer = remember { mutableStateOf("X") }
    val gameStatus = remember { mutableStateOf("Game In Progress") }

    // Handle cell click
    fun onCellClick(index: Int) {
        if (boardState.value[index] == "" && gameStatus.value == "Game In Progress") {
            val newBoardState = boardState.value.toMutableList()
            newBoardState[index] = currentPlayer.value
            boardState.value = newBoardState


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

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            if (playersList.value.isNotEmpty()) {
                Box(modifier = Modifier
                    .padding(5.dp)
                    .size(100.dp)
                   // .aspectRatio(1f)
                    .background( color = Color.White, RoundedCornerShape(13.dp)),
                    //.border(1.dp, color = Color.Black , RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = "Player 1 ${playersList.value.getOrNull(0)?.playerName ?: "Loading..."}",
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
                    //.border(1.dp, color = Color.Black, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = "Player 2 ${playersList.value.getOrNull(1)?.playerName ?: "Loading..."}",
                        style = TextStyle(
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = BabyPink)
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
