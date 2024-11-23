package com.passer.passwatch.newride.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passer.passwatch.core.util.formatTime
import com.passer.passwatch.newride.domain.NewRideEvent
import com.passer.passwatch.newride.domain.NewRideState
import com.passer.passwatch.core.CustomButton

@Composable
fun NewRideScreen(
    state: NewRideState,
    onEvent: (NewRideEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828)) // Apply consistent dark background
            .padding(16.dp) // Add padding for better layout
    ) {
        // Use CustomButton for the "Start Ride" and "Stop Ride" buttons
        if (!state.rideStarted) {
            CustomButton(
                text = "Start Ride",
                onClick = { onEvent(NewRideEvent.StartRide) }
            )
        } else {
            CustomButton(
                text = "Stop Ride",
                onClick = { onEvent(NewRideEvent.StopRide) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp)) // Add space between button and text

        // Display ride information with white text for better readability
        Text(
            text = "You are on Ride ${state.rideId}",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Elapsed Time: ${formatTime(state.rideTime)}",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
