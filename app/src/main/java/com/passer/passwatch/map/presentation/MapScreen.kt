package com.passer.passwatch.map.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
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
                state = viewModel.mapState,
                onEvent = { event -> viewModel.onEvent(event) }
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
    state: MapState,
    onEvent: (MapEvent) -> Unit
) {
    val context = LocalContext.current

    // Determine the initial camera position based on the last Route
    val lastRoute = state.routes.lastOrNull()
    val initialCameraPosition = lastRoute?.let {
        LatLng(it.latitude, it.longitude)
    } ?: LatLng(0.0, 0.0) // Default to (0.0, 0.0) if no routes are available

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            initialCameraPosition,
            15f
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Map showing routes and near passes
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            cameraPositionState = cameraPositionState
        ) {
            // Plot polylines for routes
            val routesCoordinates = state.routes.map { LatLng(it.latitude, it.longitude) }
            if (routesCoordinates.size > 1) {
                Polyline(
                    points = routesCoordinates
                )
            }

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
                .fillMaxHeight(0.25f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.nearPasses) { nearPass ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Time: ${nearPass.time}")
                        Text(text = "Latitude: ${nearPass.latitude}")
                        Text(text = "Longitude: ${nearPass.longitude}")
                        Text(text = "Distance: ${nearPass.distance}")
                        Text(text = "Speed: ${nearPass.speed}")
                    }
                }
            }
        }

        // List of Geo Waypoints (Routes)
        Text(text = "Geo Waypoints", fontSize = 20.sp)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.routes) { route ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Time: ${route.time}")
                        Text(text = "Latitude: ${route.latitude}")
                        Text(text = "Longitude: ${route.longitude}")
                        Text(text = "Speed: ${route.speed}")
                    }
                }
            }
        }
    }
}
