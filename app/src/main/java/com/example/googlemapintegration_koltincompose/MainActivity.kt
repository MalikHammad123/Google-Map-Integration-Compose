package com.example.googlemapintegration_koltincompose

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.googlemapintegration_koltincompose.ui.theme.GoogleMapIntegrationKoltinComposeTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoogleMapIntegrationKoltinComposeTheme {
                MapScreen()
            }
        }
    }
}

@SuppressLint("MissingPermission") // Permission must be granted externally
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    // Fetch user's current location
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLatLng = LatLng(it.latitude, it.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng!!, 15f)
                //fetchAndLogAddress(context, currentLatLng!!) // Log address for the initial location
            }
        }
    }

    var uiSettings by remember {
        mutableStateOf(MapUiSettings(zoomControlsEnabled = true))
    }
    var properties by remember {
        mutableStateOf(MapProperties(mapType = MapType.TERRAIN))
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
        onMapClick = { latLng ->
            currentLatLng = latLng
            fetchAndLogAddress(context, latLng) // Call the helper function
        }
    ) {
        currentLatLng?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Your Location"
            )
        }
    }
}

/**
 * Fetches and logs the address for a given LatLng.
 *
 * @param context The context to use for Geocoder initialization.
 * @param latLng The LatLng for which to fetch the address.
 */
private fun fetchAndLogAddress(context: Context, latLng: LatLng) {
    val geocoder = Geocoder(context)
    try {
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0].getAddressLine(0)
            Log.d("MapScreen", "Selected Address: $address")
        } else {
            Log.d("MapScreen", "No address found for the selected location.")
        }
    } catch (e: Exception) {
        Log.e("MapScreen", "Error fetching address: ${e.message}")
    }
}


@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    GoogleMapIntegrationKoltinComposeTheme {
        MapScreen()
    }
}