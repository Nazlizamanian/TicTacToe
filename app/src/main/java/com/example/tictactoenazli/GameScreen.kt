package com.example.tictactoenazli

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun GameScreen(navController: NavController){

    Column (modifier = Modifier.fillMaxSize()){
        Button(onClick = {navController.navigate(Screen.LobbyScreen.route)}) {
            Text("Go back")
        }
    }

}