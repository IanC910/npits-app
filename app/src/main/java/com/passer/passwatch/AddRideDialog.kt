package com.passer.passwatch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRideDialog(
    state: RideState,
    onEvent: (RideEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicAlertDialog(
        onDismissRequest = {
            onEvent(RideEvent.HideDialog)
        },
        modifier = modifier
    ) {
        Text(text = "Add ride")

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = state.startTime,
                onValueChange = {
                    onEvent(RideEvent.SetStartTime(it.toLongOrNull()))
                },
                placeholder = {
                    Text(text = "start time")
                }
            )
            TextField(
                value = state.endTime,
                onValueChange = {
                    onEvent(RideEvent.SetEndTime(it.toLongOrNull()))
                },
                placeholder = {
                    Text(text = "end time")
                }
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = {
                    onEvent(RideEvent.SaveRide)
                }) {
                    Text(text = "Save")
                }
            }
        }
    }
}
