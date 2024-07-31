package com.passer.passwatch.screen

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
import com.passer.passwatch.AddNearPassDialog
import com.passer.passwatch.AddRideDialog
import com.passer.passwatch.NearPassEvent
import com.passer.passwatch.RideEvent
import com.passer.passwatch.RideState
import kotlin.reflect.KFunction1

@Composable
fun MapScreen(
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
    ){padding ->

        if(state.isAddingRide){
            AddRideDialog(state = state, onEvent = onEvent)
        }

        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    "Rides",
                    style = MaterialTheme.typography.headlineLarge,
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
                        Text(text = "Start Time: ${ride.startTime}")
                        Text(text = "End Time: ${ride.endTime}")
                    }
                    IconButton(onClick = {
                        onEvent(RideEvent.DeleteRide(ride))
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

