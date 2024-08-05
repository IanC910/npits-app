package com.passer.passwatch.settings.presentation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

        Row {
            Button(onClick = {
                onEvent(SettingsEvent.StartScan)
            }) {
                Text("Start Scan")
            }

            Button(onClick = {
                onEvent(SettingsEvent.StopScan)
            }) {
                Text("Stop Scan")
            }
        }
        // scan for BLE devices
        BleScannerResultsBox(
            state = state,
            onSelect = {
                Log.d("SettingsScreen", "Selected device: ${it.address}")
                onEvent(SettingsEvent.SaveMacAddress(it.address))
            }
        )
    }
}

@Composable
fun BleScannerResultsBox(
    state: SettingsState,
    onSelect: (BluetoothDevice) -> Unit,
) {

    Row {
        Text("Scanned Devices:")
        if (state.scanning) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }
    // Display scanned devices
    LazyColumn {
        items(state.scannedDevices) { device ->
            BluetoothDeviceItem(bluetoothDevice = device, onSelect = onSelect)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceItem(
    bluetoothDevice: BluetoothDevice,
    onSelect: (BluetoothDevice) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable { onSelect(bluetoothDevice) },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            bluetoothDevice.name ?: "N/A",
            style = TextStyle(fontWeight = FontWeight.Normal),
        )
        Text(bluetoothDevice.address)
        val state = when (bluetoothDevice.bondState) {
            BluetoothDevice.BOND_BONDED -> "Paired"
            BluetoothDevice.BOND_BONDING -> "Pairing"
            else -> "None"
        }
        Text(text = state)
    }
}

