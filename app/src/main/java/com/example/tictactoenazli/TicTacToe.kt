package com.example.tictactoenazli

import androidx.compose.material3.ListItem
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.asStateFlow



@Composable
fun TicTacToe2() {
    val navController = rememberNavController()
    val model = GameModel()
    model.initGame()

//    val players by playerMap.asStateFlow().collectAsStateWithLifecycle()
//    val games by gameMap.asStateFlow().collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = "player") {
        composable("player") { NewPlayerScreen(navController, model) }
        composable("lobby") { LobbyScreen(navController, model) }
        composable("game/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            GameScreen(navController, model, gameId)
        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to Robins TicTacToe!")

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Enter your name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { if (playerName.isNotBlank()) {
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
                        Log.e("RobinError", "Error creating player: ${error.message}")
                    }
                } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Player")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController, model: GameModel) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    LaunchedEffect(games) {
        games.forEach { (gameId, game) ->
            // TODO: Popup with accept invite?
            if ((game.player1Id == model.localPlayerId.value || game.player2Id == model.localPlayerId.value) && game.gameState == "player1_turn") {
                navController.navigate("game/${gameId}")
            }
        }
    }

    var playerName = "Unknown?"
    players[model.localPlayerId.value]?.let {
        playerName = it.name
    }

    Scaffold(
        topBar = { TopAppBar(title =  { Text("TicTacToe - $playerName")}) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(players.entries.toList()) { (documentId, player) ->
                if (documentId != model.localPlayerId.value) { // Don't show yourself'

                    ListItem(
                        headlineText = {
                            Text("Player Name: ${player.name}")
                        },
                        supportingText = {
                            Text("Status: ...")
                        },
                        trailingContent = {
                            var hasGame = false
                            games.forEach { (gameId, game) ->
                                if (game.player1Id == model.localPlayerId.value && game.gameState == "invite") {
                                    Text("Waiting for accept...")
                                    hasGame = true
                                } else if (game.player2Id == model.localPlayerId.value && game.gameState == "invite") {
                                    Button(onClick = {
                                        model.db.collection("games").document(gameId)
                                            .update("gameState", "player1_turn")
                                            .addOnSuccessListener {
                                                navController.navigate("game/${gameId}")
                                            }
                                            .addOnFailureListener {
                                                Log.e(
                                                    "RobinError",
                                                    "Error updating game: $gameId"
                                                )
                                            }
                                    }) {
                                        Text("Accept invite")
                                    }
                                    hasGame = true
                                }
                            }
                            if (!hasGame) {
                                Button(onClick = {
                                    model.db.collection("games")
                                        .add(Game(gameState = "invite", player1Id = model.localPlayerId.value!!, player2Id = documentId))
                                        .addOnSuccessListener { documentRef ->
                                            // TODO: Navigate?
                                        }
                                }) {
                                    Text("Challenge")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController, model: GameModel, gameId: String?) {
    val players by model.playerMap.asStateFlow().collectAsStateWithLifecycle()
    val games by model.gameMap.asStateFlow().collectAsStateWithLifecycle()

    if (gameId != null && games.containsKey(gameId)) {
        Scaffold(
            topBar = { TopAppBar(title =  { Text("TicTacToe - $gameId")}) }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {

                Text("Game state: ${games[gameId]!!.gameState}")
                Text("Player 1: ${players[games[gameId]!!.player1Id]!!.name}")
                Text("Player 2: ${players[games[gameId]!!.player2Id]!!.name}")

                for (i in 0..2) {
                    Row {
                        for (j in 0..2) {
                            Button(onClick = { }) {
                                Text("Cell $i x $j")
                            }
                        }
                    }
                }

            }
        }
    } else {
        Log.e(
            "RobinError",
            "Error Game not found: $gameId"
        )
        navController.navigate("lobby")
    }
}
//hhhhh