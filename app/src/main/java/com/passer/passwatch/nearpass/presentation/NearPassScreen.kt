package com.passer.passwatch.nearpass.presentation

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
import androidx.compose.ui.unit.dp
import com.passer.passwatch.nearpass.domain.NearPassEvent
import com.passer.passwatch.nearpass.domain.NearPassState
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

        Column {
            Text(
                "Near Passes for Ride ${state.rideId}",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(20.dp)
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
                            Text(text = "Time: ${formatTimestamp(nearPass.time!!)}")
                            Text(text = "Latitude: ${nearPass.latitude}")
                            Text(text = "Longitude: ${nearPass.longitude}")
                            Text(text = "Distance: ${nearPass.distance}")
                            Text(text = "Speed: ${nearPass.speed}")
                        }
                        IconButton(onClick = {
                            onEvent(NearPassEvent.DeleteNearPass(nearPass))
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete near pass"
                            )
                        }
                    }
                }
            }
        }
    }
}


