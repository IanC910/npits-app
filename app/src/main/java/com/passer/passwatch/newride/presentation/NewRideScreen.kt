package com.passer.passwatch.newride.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.passer.passwatch.core.util.formatTime
import com.passer.passwatch.newride.domain.NewRideEvent
import com.passer.passwatch.newride.domain.NewRideState

@Composable
fun NewRideScreen(
    state: NewRideState,
    onEvent: (NewRideEvent) -> Unit,
) {
    Column {
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
}


