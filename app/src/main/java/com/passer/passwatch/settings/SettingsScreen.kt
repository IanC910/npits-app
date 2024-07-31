package com.passer.passwatch.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.passer.passwatch.core.repo.UserPreferencesRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(userPreferencesRepository: UserPreferencesRepository) {
    val scope = rememberCoroutineScope()

    val hubMacAddress by userPreferencesRepository.hubMacAddress.collectAsState(
        initial = ""
    )

    var newHubMacAddress by remember { mutableStateOf(hubMacAddress) }

    Column {
        // Display current MAC address
        Text("Current Hub MAC Address: $hubMacAddress")

        // Input field for new MAC address
        OutlinedTextField(
            value = newHubMacAddress,
            onValueChange = { newHubMacAddress = it },
            label = { Text("Enter new Hub MAC Address") }
        )

        // Button to save new MAC address
        Button(onClick = {
            scope.launch { // Use appropriate coroutine scope
                userPreferencesRepository.saveHubMacAddress(newHubMacAddress)
            }
        }) {
            Text("Save")
        }
    }
}

