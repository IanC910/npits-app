package com.passer.passwatch.settings.presentation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.passer.passwatch.core.CustomButton
import com.passer.passwatch.settings.domain.SettingsEvent
import com.passer.passwatch.settings.domain.SettingsState

@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    navController: NavHostController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display current MAC address
        Text("Current Hub MAC Address: ${state.hubMacAddress}", modifier = Modifier.padding(bottom = 16.dp))

        // Input field for new MAC address
        OutlinedTextField(
            value = state.newHubMacAddress,
            onValueChange = { onEvent(SettingsEvent.SetMacAddress(it)) },
            label = { Text("Enter new Hub MAC Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Custom buttons for Save, Start Scan, Stop Scan, and Live Telemetry
        CustomButton(
            text = "Save",
            onClick = { onEvent(SettingsEvent.SaveMacAddress(state.newHubMacAddress)) }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CustomButton(
                text = "Start Scan",
                onClick = { onEvent(SettingsEvent.StartScan) },
                modifier = Modifier.fillMaxWidth(0.4f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CustomButton(
                text = "Stop Scan",
                onClick = { onEvent(SettingsEvent.StopScan) },
                modifier = Modifier.fillMaxWidth(0.4f)
            )
        }

        CustomButton(
            text = "Live Telemetry",
            onClick = {
                navController.navigate(
                    com.passer.passwatch.core.TelemetryScreen(
                        macAddress = state.hubMacAddress,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // BLE devices scanning section
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Scanned Devices:")
        if (state.scanning) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
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
            .fillMaxWidth()
            .clickable { onSelect(bluetoothDevice) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = bluetoothDevice.name ?: "Unknown Device",
                style = TextStyle(fontWeight = FontWeight.Bold)
            )
            Text(text = bluetoothDevice.address)
        }
        Text(
            when (bluetoothDevice.bondState) {
                BluetoothDevice.BOND_BONDED -> "Paired"
                BluetoothDevice.BOND_BONDING -> "Pairing"
                else -> "Not Paired"
            }
        )
    }
}
