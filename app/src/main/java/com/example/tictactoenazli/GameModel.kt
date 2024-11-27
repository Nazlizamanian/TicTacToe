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


    fun checkWinner(board: List<Int>): Boolean {
        var win = true

        if (board[0] == 1 && board[1] == 1 && board[2] == 1) win = true //rader
        if (board[3] == 1 && board[4] == 1 && board[5] == 1) win = true
        if (board[6] == 1 && board[7] == 1 && board[8] == 1) win = true

        if (board[0] == 1 && board[3] == 1 && board[6] == 1) win = true // KOlumn
        if (board[1] == 1 && board[4] == 1 && board[7] == 1) win = true
        if (board[2] == 1 && board[5] == 1 && board[8] == 1) win = true

        if (board[0] == 1 && board[4] == 1 && board[8] == 1) win = true //// Diagonal
        if (board[2] == 1 && board[4] == 1 && board[6] == 1) win = true

        return win
    }
}
//