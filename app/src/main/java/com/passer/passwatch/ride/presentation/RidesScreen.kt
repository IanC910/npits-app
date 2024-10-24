package com.passer.passwatch.ride.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.passer.passwatch.core.MapScreen
import com.passer.passwatch.core.NearPassScreen
import com.passer.passwatch.ride.domain.RideEvent
import com.passer.passwatch.ride.domain.RideState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RidesScreen(
    navController: NavController,
    state: RideState,
    onEvent: (RideEvent) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onEvent(RideEvent.ShowDialog)
                }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add ride",
                )
            }
        }
    ) { padding ->

        if (state.isAddingRide) {
            AddRideDialog(state = state, onEvent = onEvent)
        }

        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF282828)), // Apply consistent dark background
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    "Rides",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White, // White text for visibility
                    modifier = Modifier.padding(20.dp)
                )
            }

            items(state.rides) { ride ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Ride #${ride.id}", fontSize = 20.sp, color = Color.White) // White text
                        Text(text = "Start Time: ${ride.startTime?.let { formatTimestamp(it) }}", color = Color.White) // White text
                        Text(text = "End Time: ${ride.endTime?.let { formatTimestamp(it) }}", color = Color.White) // White text
                    }
                    IconButton(onClick = {
                        navController.navigate(
                            MapScreen(
                                rideId = ride.id
                            )
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View Map",
                            tint = Color.White // White icon for visibility
                        )
                    }
                    IconButton(onClick = {
                        navController.navigate(
                            NearPassScreen(
                                rideId = ride.id
                            )
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "View Near Passes",
                            tint = Color.White // White icon for visibility
                        )
                    }
                    IconButton(onClick = {
                        onEvent(RideEvent.DeleteRide(ride))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete ride",
                            tint = Color.White // White icon for visibility
                        )
                    }
                }
            }
        }
    }
}

// Utility function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}