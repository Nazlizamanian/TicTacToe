package com.example.tictactoenazli


sealed class Screen(val route: String){

    object GameScreen: Screen(route = "game")
    object LobbyScreen: Screen(route = "lobby")
    object NewPlayerScreen : Screen(route = "player")

}
