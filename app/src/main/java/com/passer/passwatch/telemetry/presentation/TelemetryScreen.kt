package com.passer.passwatch.telemetry.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passer.passwatch.telemetry.domain.TelemetryEvent
import com.passer.passwatch.telemetry.domain.TelemetryState

@Composable
fun TelemetryScreen(
    state: TelemetryState,
    onEvent: (TelemetryEvent) -> Unit,
) {
    Column {
        Text("Telemetry for ${state.macAddress}")
        Button(onClick = {
            onEvent(TelemetryEvent.ConnectDevice)
        }) {
            Text("Connect to Device")
        }

        LazyColumn {
            items(state.services){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "${it.uuid}", fontSize = 20.sp)

                        for(characteristic in it.characteristics){
                            Text(text = "${characteristic.uuid}", fontWeight = FontWeight.Bold)
                            Text(text = "${state.characteristicValue[characteristic.uuid.toString()]}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }



}