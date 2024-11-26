package com.passer.passwatch.settings.presentation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Selected Hub MAC Address: ${state.hubMacAddress}",
                modifier = Modifier.padding(bottom = 16.dp))
        }

        OutlinedTextField(
            value = state.newHubMacAddress,
            onValueChange = { onEvent(SettingsEvent.SetMacAddress(it)) },
            label = { Text("Enter new Hub MAC Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        CustomButton(
            text = "Save Manually Entered MAC Address",
            onClick = { onEvent(SettingsEvent.SaveMacAddress(state.newHubMacAddress)) },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CustomButton(
            text = if (state.scanning) "Scanning devices..." else "Scan for devices",
            onClick = {
                if (state.scanning) {
                    onEvent(SettingsEvent.StopScan)
                } else {
                    onEvent(SettingsEvent.StartScan)
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CustomButton(
            text = "Connect to selected device",
            onClick = {
                onEvent(SettingsEvent.Connect)
            }
        )
        Text(
            text = state.connectionState,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CustomButton(
            text = "Sync from Hub",
            onClick = {
                onEvent(SettingsEvent.SyncData)
            }
        )
        Text(
            text = state.syncStatus,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CustomButton(
            text = "Live Telemetry",
            onClick = {
                navController.navigate(
                    com.passer.passwatch.core.TelemetryScreen(
                        macAddress = state.hubMacAddress,
                    )
                )
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
