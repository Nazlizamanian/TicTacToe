package com.example.tictactoenazli

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tictactoenazli.ui.theme.BabyPink


@Composable
fun TicTacToe() {
    val navController = rememberNavController()
    val model = GameModel()
    model.initGame()

//    val players by playerMap.asStateFlow().collectAsStateWithLifecycle()
//    val games by gameMap.asStateFlow().collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Screen.NewPlayerScreen.route) {
        composable(Screen.NewPlayerScreen.route) {
            NewPlayerScreen(navController, model)
        }

        composable(Screen.LobbyScreen.route) {
            LobbyScreen(navController, model)
        }
        composable("${Screen.GameScreen.route}/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            GameScreen(navController, model, gameId)
        }
        /*
        composable(Screen.ResultScreen.route) {
            ResultScreen(navController, model)
        }
        composable(Screen.MainScreen.route) {
            MainScreen(navController, model)
        }*/
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPlayerScreen(navController: NavController, model: GameModel) {
    val sharedPreferences = LocalContext.current.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)

    // Check for playerId in SharedPreferences
    LaunchedEffect(Unit) {
        model.localPlayerId.value = sharedPreferences.getString("playerId", null)
        if (model.localPlayerId.value != null) {
            navController.navigate("lobby")
        }
    }

    if (model.localPlayerId.value == null) {

        var playerName by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BabyPink)
                .padding(16.dp),

            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome to TicTacToe", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold))

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Enter your name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(13.dp))
                    .padding(8.dp),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    containerColor = Color.White, //bakgrund
                    focusedIndicatorColor = Color.Blue,
                    unfocusedIndicatorColor = Color.Gray
                )
            )


            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .clickable {
                        if (playerName.isNotBlank()) {
                            // Create new player in Firestore
                            val newPlayer = Player(name = playerName)
                            model.db.collection("players").add(newPlayer).addOnSuccessListener { documentRef ->
                                val newPlayerId = documentRef.id
                                // Save playerId in SharedPreferences
                                sharedPreferences.edit().putString("playerId", newPlayerId).apply()
                                // Update local variable and navigate to lobby
                                model.localPlayerId.value = newPlayerId
                                navController.navigate("lobby")
                            }.addOnFailureListener { error ->
                                Log.e("Error", "Error creating player: ${error.message}")
                            }
                        }
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Create Player", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }

        }
    }
}




//hhhhh