package com.passer.passwatch.core

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WideButton(
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