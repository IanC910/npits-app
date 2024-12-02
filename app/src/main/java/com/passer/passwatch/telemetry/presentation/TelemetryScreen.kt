package com.passer.passwatch.telemetry.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passer.passwatch.core.WideButton
import com.passer.passwatch.telemetry.domain.TelemetryEvent
import com.passer.passwatch.telemetry.domain.TelemetryState

@Composable
fun TelemetryScreen(
    state: TelemetryState,
    onEvent: (TelemetryEvent) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828)) // Set background color
            .padding(16.dp) // Add padding
    ) {
        // Displaying the MAC address with better spacing and formatting
        Text(
            text = "Telemetry for ${state.macAddress}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // CustomButton for device connection
        WideButton(
            text = "Connect to Device",
            onClick = { onEvent(TelemetryEvent.ConnectDevice) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // LazyColumn to display services and characteristics
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.services) { service ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Service UUID: ${service.uuid}",
                            fontSize = 20.sp,
                            color = Color.White
                        )

                        // Displaying characteristics
                        service.characteristics.forEach { characteristic ->
                            Text(
                                text = "Characteristic UUID: ${characteristic.uuid}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = state.characteristicValue[characteristic.uuid.toString()] ?: "N/A",
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
