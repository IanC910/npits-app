package com.passer.passwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.passer.passwatch.core.MapScreen
import com.passer.passwatch.core.RidesScreen
import com.passer.passwatch.core.SettingsScreen
import com.passer.passwatch.core.ble.UUIDConstants

@Composable
fun MainScreen(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Button(
            onClick = {
                navController.navigate(com.passer.passwatch.core.NewRideScreen)
            }
        ) {
            Text(text = "New Ride")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate(RidesScreen)
            }
        ) {
            Text(text = "Rides")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate(MapScreen)
            }
        ) {
            Text(text = "Map")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate(SettingsScreen)
            }
        ) {
            Text(text = "Settings")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}