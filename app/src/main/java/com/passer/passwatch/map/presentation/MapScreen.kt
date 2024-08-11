package com.passer.passwatch.map.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passer.passwatch.map.domain.MapEvent
import com.passer.passwatch.map.domain.MapState

@Composable
fun MapScreen(
    state: MapState,
    onEvent: (MapEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(text = "Near Passes", fontSize = 20.sp)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),

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
                .fillMaxHeight(0.5f),
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