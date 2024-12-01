package com.example.tictactoenazli

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow

//vår viewmodel som asvarig för vår data
class GameModel: ViewModel() {

    val db = Firebase.firestore
    var localPlayerId = mutableStateOf<String?>(null)
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap())
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())

    fun initGame() {
        // Listen for players
        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Player::class.java)!!
                    }
                    playerMap.value = updatedMap
                }
            }

        // Listen for games
        db.collection("games")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    val updatedMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Game::class.java)!!
                    }
                    gameMap.value = updatedMap
                }
            }
    }

    fun updateScore(winnerId: String) {
        // Get the winner player from the database and increment their score
        db.collection("players").document(winnerId)
            .get()
            .addOnSuccessListener { document ->
                val player = document.toObject(Player::class.java)
                if (player != null) {
                    val updatedPlayer = player.copy(score = player.score + 1)
                    // Update the player's score in Firestore
                    db.collection("players").document(winnerId).set(updatedPlayer)
                }
            }
    }


    fun checkWinner(gameId: String?, board: List<Int>): Int {
        //rader
        for (i in 0..2) {
            if (board[i * 3] != 0 && board[i * 3] == board[i * 3 + 1] && board[i * 3] == board[i * 3 + 2]) {
                val winnerId = if (board[i * 3] == 1) gameMap.value[gameId]?.player1Id else gameMap.value[gameId]?.player2Id
                winnerId?.let { updateScore(it) }
                return board[i * 3]
            }
        }

        // Column
        for (i in 0..2) {
            if (board[i] != 0 && board[i] == board[i + 3] && board[i] == board[i + 6]) {
                val winnerId = if (board[i] == 1) gameMap.value[gameId]?.player1Id else gameMap.value[gameId]?.player2Id
                winnerId?.let { updateScore(it) } //uppdaterar poäng om man vinner kolumn.
                return board[i] // Return the winner (1 or 2)
            }
        }

        //Diagonal
        if (board[0] != 0 && board[0] == board[4] && board[0] == board[8]) {
            val winnerId = if (board[0] == 1) gameMap.value[gameId]?.player1Id else gameMap.value[gameId]?.player2Id
            winnerId?.let { updateScore(it) }//uppdaterar poäng om man vinner diagonal
            return board[0]
        }
        if (board[2] != 0 && board[2] == board[4] && board[2] == board[6]) {
            val winnerId = if (board[2] == 1) gameMap.value[gameId]?.player1Id else gameMap.value[gameId]?.player2Id
            winnerId?.let { updateScore(it) } //Andra hållet diagonal
            return board[2]
        }

        // If all cells are filled and there is no winner, it's a draw
        if (board.none { it == 0 }) {//Här uppdateras inte updateScore ingen får poäng vid draw.
            return -1 // Return -1 to indicate a draw
        }

        return 0   // If no winner, return 0 (no winner)
    }



    fun checkGameState(gameId: String?, cell: Int) {
        if (gameId != null) {
            val game: Game? = gameMap.value[gameId]
            if (game != null) {
                // Check if it's the player's turn
                val myTurn = game.gameState == "player1_turn" && game.player1Id == localPlayerId.value ||
                        game.gameState == "player2_turn" && game.player2Id == localPlayerId.value
                if (!myTurn) return // It's not the player's turn, so return

                val list: MutableList<Int> = game.gameBoard.toMutableList()

                // Place the current player's move on the board
                if (game.gameState == "player1_turn") {
                    list[cell] = 1
                } else if (game.gameState == "player2_turn") {
                    list[cell] = 2
                }

                val winner = checkWinner(gameId, list)

                var turn = ""
                when (winner) {
                    1 -> turn = "player1_won"
                    2 -> turn = "player2_won"
                    -1 -> turn = "draw" //Om det är ingen vinnare.
                    0 -> {
                        // No winner yet, change turn
                        turn = if (game.gameState == "player1_turn") "player2_turn" else "player1_turn"
                    }
                }

                // Update the game state and game board in the database
                db.collection("games").document(gameId)
                    .update(
                        "gameBoard", list,
                        "gameState", turn
                    )
            }
        }
    }
}