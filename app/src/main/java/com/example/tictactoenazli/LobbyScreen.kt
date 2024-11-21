package com.example.tictactoenazli

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tictactoenazli.ui.theme.BabyPink
import com.example.tictactoenazli.ui.theme.CustomTextColor
import com.example.tictactoenazli.ui.theme.Purple80
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(navController: NavController) {
    val db = Firebase.firestore
    val playersList = remember { MutableStateFlow<List<Player>>(emptyList()) }

    // Observe players list from Firestore
    LaunchedEffect(Unit) {
        db.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                value?.let {
                    playersList.value = it.toObjects(Player::class.java)
                }
            }
    }

    // Collect the StateFlow for Composables
    val players by playersList.collectAsStateWithLifecycle()

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = BabyPink),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Players in Lobby",
                style = TextStyle(
                    fontSize = 37.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
            )

            // Display player list
            if (players.isEmpty()) {
                Text(text = "No players found", style = MaterialTheme.typography.titleLarge)
            } else {
                Button(onClick = {
                    navController.navigate(Screen.GameScreen.route)
                }) {
                    Text(text = "To Game screen")

                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(players) { player ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 13.dp)
                                .shadow(10.dp, RoundedCornerShape(13.dp)) // Mimics elevation
                                .background(Color.White, RoundedCornerShape(13.dp))
                           // elevation = CardDefaults.cardElevation(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .background(Color.White)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Player Icon",
                                    modifier = Modifier.size(40.dp),
                                    tint = BabyPink
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = player.playerName,
                                        style = TextStyle(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Score ${player.score}",
                                        style = TextStyle(fontSize = 18.sp)
                                    )
                                }
                                // Invitetation button
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = BabyPink,
                                            shape = RoundedCornerShape(13.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 12.dp)
                                        .clickable {
                                            val query = db
                                                .collection("players")
                                                .whereEqualTo("playerId", player.playerId)
                                            query
                                                .get()
                                                .addOnSuccessListener { querySnapShot ->
                                                    for (documentSnapShot in querySnapShot) {
                                                        documentSnapShot.reference.update(
                                                            "invitation",
                                                            "Hello"
                                                        )
                                                    }
                                                }
                                        }
                                ) {
                                    Text(
                                        text = "Invite",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                            }
                        }


                    }
                }

            }
        }
    }
}
