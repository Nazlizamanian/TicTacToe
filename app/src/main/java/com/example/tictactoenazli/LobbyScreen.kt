package com.example.tictactoenazli

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tictactoenazli.ui.theme.CustomTextColor
import com.example.tictactoenazli.ui.theme.Purple80



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController){

    Scaffold (
        topBar = {
            TopAppBar(title = { Text("Lobby Screen") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }

                },
                actions = {
                    IconButton(onClick = {navController.navigate(Screen.GameScreen)}) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "forward")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Purple80,
                    titleContentColor = CustomTextColor
                )
                )
        }
    ){
        padding ->
        Column (modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Button(
                onClick = {
                    navController.navigate(Screen.GameScreen.route)
                }
            ) {
                Text(text = "Start game now!")

            }
        }

    }





}

//blablanas