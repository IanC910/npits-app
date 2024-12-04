package com.passer.passwatch.nearpass.presentation

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
import com.passer.passwatch.nearpass.domain.NearPassEvent
import com.passer.passwatch.nearpass.domain.NearPassState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.passer.passwatch.ride.presentation.formatTimestamp

@Composable
fun NearPassScreen(
    state: NearPassState,
    onEvent: (NearPassEvent) -> Unit,
) {
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {
            onEvent(NearPassEvent.ShowDialog)
        }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add near pass",
            )
        }
    }) { padding ->

        if (state.isAddingNearPass) {
            AddNearPassDialog(state = state, onEvent = onEvent)
        }

        Column (
          modifier = Modifier.background(Color(0xFF282828))
        ) {
            Text(
                "Near Passes for Ride ${state.rideId}",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(20.dp),
                color = Color.White
            )

            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.nearPasses) { nearPass ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Time: ${nearPass.time?.let {formatTimestamp(it)}}")
                            Text(text = "Latitude: ${nearPass.latitude}")
                            Text(text = "Longitude: ${nearPass.longitude}")
                            Text(text = "Distance: ${nearPass.distance} cm")
                            Text(text = "Speed: ${nearPass.speed} km/h")
                        }
                        IconButton(onClick = {
                            onEvent(NearPassEvent.DeleteNearPass(nearPass))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete near pass",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}


