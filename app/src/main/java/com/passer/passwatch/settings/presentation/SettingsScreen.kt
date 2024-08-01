package com.passer.passwatch.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.passer.passwatch.settings.domain.SettingsEvent
import com.passer.passwatch.settings.domain.SettingsState

@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    Column {
        // Display current MAC address
        Text("Current Hub MAC Address: ${state.hubMacAddress}")

        // Input field for new MAC address
        OutlinedTextField(
            value = state.newHubMacAddress,
            onValueChange = { onEvent(SettingsEvent.SetMacAddress(it)) },
            label = { Text("Enter new Hub MAC Address") }
        )

        // Button to save new MAC address
        Button(onClick = {
            onEvent(SettingsEvent.SaveMacAddress(state.newHubMacAddress))
        }) {
            Text("Save")
        }
    }
}

