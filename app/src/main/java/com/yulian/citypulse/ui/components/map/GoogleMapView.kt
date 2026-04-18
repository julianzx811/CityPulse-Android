package com.yulian.citypulse.ui.components.map

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.yulian.citypulse.BuildConfig

@Composable
fun GoogleMapView(lat: Double, lng: Double, weatherMain: String, isNight: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = { mapView.apply { onCreate(null) } },
        modifier = Modifier.fillMaxSize(),
        update = { mv ->
            mv.getMapAsync { map ->
                setupMap(map, lat, lng, weatherMain, isNight, context)
            }
        }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

fun setupMap(
    map: GoogleMap,
    lat: Double,
    lng: Double,
    weatherMain: String,
    isNight: Boolean,
    context: Context
) {
    val location = LatLng(lat, lng)

    if (isNight) {
        try {
            map.setMapStyle(
                MapStyleOptions("""
                [
                  {"elementType":"geometry","stylers":[{"color":"#242f3e"}]},
                  {"elementType":"labels.text.fill","stylers":[{"color":"#746855"}]},
                  {"elementType":"labels.text.stroke","stylers":[{"color":"#242f3e"}]},
                  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#38414e"}]},
                  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#212a37"}]},
                  {"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#9ca5b3"}]},
                  {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#746855"}]},
                  {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1f2835"}]},
                  {"featureType":"road.highway","elementType":"labels.text.fill","stylers":[{"color":"#f3d19c"}]},
                  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#17263c"}]},
                  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#515c6d"}]},
                  {"featureType":"poi","elementType":"geometry","stylers":[{"color":"#283d6a"}]},
                  {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#2f3948"}]}
                ]
                """.trimIndent())
            )
        } catch (e: Exception) { e.printStackTrace() }
    }

    map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))

    val markerSnippet = when (weatherMain.lowercase()) {
        "clear" -> "Despejado"
        "clouds" -> "Nublado"
        "rain", "drizzle" -> "Lluvia"
        "thunderstorm" -> "Tormenta"
        "snow" -> "Nieve"
        else -> "Medellín"
    }

    map.addMarker(
        MarkerOptions()
            .position(location)
            .title("Medellín")
            .snippet(markerSnippet)
    )

    if (weatherMain.lowercase() in listOf("rain", "drizzle", "thunderstorm")) {
        val tileProvider = object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): java.net.URL? {
                return try {
                    java.net.URL(
                        "https://tile.openweathermap.org/map/precipitation_new/$zoom/$x/$y.png?appid=${BuildConfig.OPENWEATHER_API_KEY}"
                    )
                } catch (e: Exception) { null }
            }
        }
        map.addTileOverlay(
            TileOverlayOptions()
                .tileProvider(tileProvider)
                .transparency(0.3f)
        )
    }

    map.uiSettings.isZoomControlsEnabled = true
    map.uiSettings.isCompassEnabled = true
}