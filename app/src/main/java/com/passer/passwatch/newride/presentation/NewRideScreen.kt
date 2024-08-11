package com.passer.passwatch.newride.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passer.passwatch.core.util.formatTime
import com.passer.passwatch.newride.domain.NewRideEvent
import com.passer.passwatch.newride.domain.NewRideState

@Composable
fun NewRideScreen(
    state: NewRideState,
    onEvent: (NewRideEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.15f)
        ) {
            if (!state.rideStarted) {
                Button(onClick = { onEvent(NewRideEvent.StartRide) }) {
                    Text(text = "Start Ride")
                }

            } else {
                Button(onClick = { onEvent(NewRideEvent.StopRide) }) {
                    Text(text = "Stop Ride")
                }
                Text(text = "You are on Ride ${state.rideId}")
                Text(text = "Elapsed Time: ${formatTime(state.rideTime)}")
            }
        }

        Text(text = "Near Passes", fontSize = 20.sp)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),

            verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {
            items(state.nearPasses) { nearPass ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Time: ${nearPass.time}")
                        Text(text = "Latitude: ${nearPass.latitude}")
                        Text(text = "Longitude: ${nearPass.longitude}")
                        Text(text = "Distance: ${nearPass.distance}")
                        Text(text = "Speed: ${nearPass.speed}")
                    }
                }
            }
        }

        Text(text = "Geo Waypoints", fontSize = 20.sp)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.routes) { nearPass ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Time: ${nearPass.time}")
                        Text(text = "Latitude: ${nearPass.latitude}")
                        Text(text = "Longitude: ${nearPass.longitude}")
                        Text(text = "Speed: ${nearPass.speed}")
                    }
                }
            }

        }
    }
}


