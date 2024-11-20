package com.example.tictactoenazli


sealed class Screen(val route: String){
    object MainScreen: Screen(route = "main")

    object GameScreen: Screen(route = "game")
    object LobbyScreen: Screen(route = "lobby")

    object ResultScreen: Screen(route = "result")
}
