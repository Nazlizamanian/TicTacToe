package com.example.tictactoenazli

data class Game(
    var gameBoard: List<Int> = List(9) { 0 }, // 0: empty, 1: player1's move, 2: player2's move
    var gameState: String = "invite", // Possible values: "invite", "player1_turn", "player2_turn" "player1_won", "player2_won", "draw"
    var player1Id: String = "",
    var player2Id: String = ""
)
