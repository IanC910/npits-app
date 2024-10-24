package com.passer.passwatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.passer.passwatch.core.NewRideScreen
import com.passer.passwatch.core.RidesScreen
import com.passer.passwatch.core.SettingsScreen

@Composable
fun MainScreen(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Button(
            onClick = {
                navController.navigate(NewRideScreen)
            }
        ) {
            Text(text = "New Ride")
        }

        Button(
            onClick = {
                navController.navigate(RidesScreen)
            }
        ) {
            Text(text = "Rides")
        }

        Button(
            onClick = {
                navController.navigate(SettingsScreen)
            }
        ) {
            Text(text = "Settings")
        }

    }
}