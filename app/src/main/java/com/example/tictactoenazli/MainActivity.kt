@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tictactoenazli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue


import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tictactoenazli.ui.theme.TicTacToeNazliTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.tictactoenazli.LobbyScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            TicTacToeNazliTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = Screen.MainScreen.route ){
                        composable(route = Screen.MainScreen.route) {
                            MainScreen(navController = navController)

                        }
                        composable(route =Screen.LobbyScreen.route ) {
                            LobbyScreen(navController = navController)

                        }
                        composable(route = Screen.GameScreen.route) {
                            GameScreen(navController = navController)
                        }
                    }

                }
            }
        }
    }
}

data class Player(
    //val read only sätt 1 gång
    //använd dokument id sedan ist för sträng.
    var playerId: String ="", //var är changable,
    var playerName: String= "",
    var invitation: String = "",
    var score: Int = 0
)

@Composable
fun MainScreen(navController: NavController){
    val db = Firebase.firestore
    //hämtar ut en referens till vår databas för att kunna spara oh skriva till databasen.
    val playersList = MutableStateFlow<List<Player>>(emptyList())

    db.collection("players") //för att uppdatera listan.
        .addSnapshotListener { value, error ->
            if( error != null){
                return@addSnapshotListener
            }
            if (value != null){ //lägg in logiken här, vad ändras på skärmen.
                playersList.value = value.toObjects()
            }
        }

    val players by playersList.asStateFlow().collectAsStateWithLifecycle()

    Scaffold (modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)){
            items(players){ player->

                ListItem(
                    headlineText ={
                        Text("Name: ${player.playerName}")

                    },
                    supportingText = {
                        Text("Invite: ${player.invitation}")
                        Text("Score ${player.score}")
                                     },

                    trailingContent = {
                        Button(onClick = {
                            val query = db.collection("players").whereEqualTo("playerId", player.playerId)

                            query.get().addOnSuccessListener { querySnapShot ->
                                for(documentSnapShot in querySnapShot){
                                    documentSnapShot.reference.update( "invitation", "Hello")
                                }
                            }
                        }) {
                            Text("Invite")
                        }
                    }


                )




            }



        }
        Spacer(modifier = Modifier.padding(10.dp))
        Button(onClick = {
            navController.navigate(Screen.LobbyScreen.route)
        }) {
            Text(text = "To Lobby screen")

        }
    }
}

