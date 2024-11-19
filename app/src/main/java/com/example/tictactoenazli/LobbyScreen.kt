package com.example.tictactoenazli

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun LobbyScreen(navController: NavController){


    Column (modifier = Modifier.fillMaxSize()){
        Button(
            onClick = {
                navController.navigate(Screen.GameScreen.route)
            }
        ) {
            Text(text = "Start game now!")

        }
    }

}


