package com.example.tictactoenazli

data class Player(
    //val read only sätt 1 gång
    //använd dokument id sedan ist för sträng.
    var playerId: String ="", //var är changable,
    var name: String= "",
    var invitation: String = "",
    var score: Int = 0
)
