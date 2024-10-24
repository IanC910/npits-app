package com.passer.passwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.passer.passwatch.core.NewRideScreen
import com.passer.passwatch.core.RidesScreen
import com.passer.passwatch.core.SettingsScreen

@Composable
fun MainScreen(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
    ) {

        CustomButton(
            text = "New Ride",
            onClick = { navController.navigate(NewRideScreen) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomButton(
            text = "Rides",
            onClick = { navController.navigate(RidesScreen) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomButton(
            text = "Settings",
            onClick = { navController.navigate(SettingsScreen) }
        )

        Spacer(modifier = Modifier.height(16.dp))

    }
}

// Reusable button function with customizable parameters
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0xFF8A529B),
    contentColor: Color = Color.White,

    modifier: Modifier = Modifier
        .fillMaxWidth(0.8f)
        .height(50.dp)
) {

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        modifier = modifier
    ) {
        Text(text = text)
    }
}
