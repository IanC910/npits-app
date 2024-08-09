package com.passer.passwatch.nearpass.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passer.passwatch.nearpass.domain.NearPassEvent
import com.passer.passwatch.nearpass.domain.NearPassState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNearPassDialog(
    state: NearPassState,
    onEvent: (NearPassEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicAlertDialog(
        onDismissRequest = {
            onEvent(NearPassEvent.HideDialog)
        },
        modifier = modifier
    ) {
        Text(text = "Add near pass")

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = state.time,
                onValueChange = {
                    onEvent(NearPassEvent.SetTime(it))
                },
                placeholder = {
                    Text(text = "time")
                }
            )
            TextField(
                value = state.latitude,
                onValueChange = {
                    onEvent(NearPassEvent.SetLatitude(it))
                },
                placeholder = {
                    Text(text = "latitude")
                }
            )
            TextField(
                value = state.longitude,
                onValueChange = {
                    onEvent(NearPassEvent.SetLongitude(it))
                },
                placeholder = {
                    Text(text = "longitude")
                }
            )
            TextField(
                value = state.distance,
                onValueChange = {
                    onEvent(NearPassEvent.SetDistance(it))
                },
                placeholder = {
                    Text(text = "distance")
                }
            )
            TextField(
                value = state.speed,
                onValueChange = {
                    onEvent(NearPassEvent.SetSpeed(it))
                },
                placeholder = {
                    Text(text = "speed")
                }
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = {
                    onEvent(NearPassEvent.SaveNearPass)
                }) {
                    Text(text = "Save")
                }
            }
        }
    }
}