package com.example.tictactoenazli

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow

//vår viewmodel som asvarig för vår data
class GameModel: ViewModel() {

    val db = Firebase.firestore //Så kan interagera med db, realtidsdatabas i firebase.
    var localPlayerId = mutableStateOf<String?>(null) //representera lokala spelare Id

    //Obs listor över spelare o spel i realtid. kartor updateras dynamiskt
    // UI reagerar på förädrningar i data utan att behöva hämta elr upd dem
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap())
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())

    fun initGame() {
        // Listen for players
        db.collection("players")
            //Lyssnar o triggas varje gång data i kollektion ändras
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) { //Upd playerMap konvertera t player-instans o tilldela unik documentId
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

    //Uppdatera poäng i spelet.
    fun updateScore(winnerId: String) {
        db.collection("players").document(winnerId)
            .get()
            .addOnSuccessListener { document ->
                val player = document.toObject(Player::class.java)
                if (player != null) {
                    //skapa 1 kopia lägg till 1 poäng sedan tbx t db med updaterade scores
                    val updatedPlayer = player.copy(score = player.score + 1)
                    db.collection("players").document(winnerId).set(updatedPlayer)
                }
            }
    }


    fun checkWinner(gameId: String?, board: List<Int>): Int {
        //rader
        for (i in 0..2) {
            if (board[i * 3] != 0 && board[i * 3] == board[i * 3 + 1] && board[i * 3] == board[i * 3 + 2]) {
                val winnerId = if (board[i * 3] == 1) gameMap.value[gameId]?.player1Id else gameMap.value[gameId]?.player2Id
                winnerId?.let { updateScore(it) } //Sätt winnerID t player1 elr 2 beroende på om de var 1,2
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
        if (gameId == null) return

        val game = gameMap.value[gameId] ?: return

        // Kontrollera om det är spelarens tur
        val isPlayer1Turn = game.gameState == "player1_turn" && game.player1Id == localPlayerId.value
        val isPlayer2Turn = game.gameState == "player2_turn" && game.player2Id == localPlayerId.value
        if (!(isPlayer1Turn || isPlayer2Turn)) return

        // Kontrollera om cellen är tom  om redan är markerad av en spelare
        if (game.gameBoard[cell] != 0) return

        // Uppdatera spelbrädet med spelarens drag
        val updatedBoard = game.gameBoard.toMutableList()
        updatedBoard[cell] = if (isPlayer1Turn) 1 else 2

        // Kontrollera vinnare eller oavgjort
        val winner = checkWinner(gameId, updatedBoard)
        val newGameState = when (winner) {
            1 -> "player1_won"
            2 -> "player2_won"
            -1 -> "draw" // Oavgjort
            else -> if (isPlayer1Turn) "player2_turn" else "player1_turn" // Nästa spelares tur
        }

        // Uppdatera databasen
        db.collection("games").document(gameId)
            .update(
                "gameBoard", updatedBoard,
                "gameState", newGameState
            )
    }

}