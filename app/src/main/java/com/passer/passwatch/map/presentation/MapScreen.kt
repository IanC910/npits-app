package com.passer.passwatch.map.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.passer.passwatch.core.util.epochToUTC
import com.passer.passwatch.map.domain.MapEvent
import com.passer.passwatch.map.domain.MapState

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            val viewModel: MapViewModel by viewModels()
            MapScreen(
                state = viewModel.mapState
            )
        }
    }
}

class MapViewModel : ViewModel() {
    private val _mapState = mutableStateOf(MapState())
    val mapState: MapState get() = _mapState.value

    fun onEvent(event: MapEvent) {
        // Handle map events here
    }
}

@Composable
fun MapScreen(
    state: MapState
) {
    LocalContext.current

    val cameraPositionState = rememberCameraPositionState()

    // Update the camera position after composition
    LaunchedEffect(state.routes) {
        val firstNearPass = state.nearPasses.firstOrNull()

        if (firstNearPass?.latitude != null && firstNearPass.longitude != null) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(firstNearPass.latitude!!,
                firstNearPass.longitude!!
            ), 15f))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF282828)),
    ) {
        // Map showing routes and near passes
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            cameraPositionState = cameraPositionState
        ) {
            // Plot markers for near passes
            val nearPassCoordinates = state.nearPasses.mapNotNull { nearPass ->
                nearPass.latitude?.let { lat ->
                    nearPass.longitude?.let { lon ->
                        LatLng(lat, lon)
                    }
                }
            }
            nearPassCoordinates.forEach { coordinate ->
                Marker(
                    position = coordinate,
                    title = "Near Pass at ${coordinate.latitude}, ${coordinate.longitude}"
                )
            }
        }

        // List of Near Passes
        Text(text = "Near Passes", fontSize = 20.sp)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.nearPasses) { nearPass ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Time: ${epochToUTC(nearPass.time?.div(1000))}")
                        Text(text = "Latitude: ${nearPass.latitude}")
                        Text(text = "Longitude: ${nearPass.longitude}")
                        Text(text = "Distance: ${nearPass.distance}")
                        Text(text = "Speed: ${nearPass.speed}")
                    }
                }
            }
        }
    }
}
