package com.yulian.citypulse.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.yulian.citypulse.data.model.WeatherResponse
import com.yulian.citypulse.ui.theme.Grey900
import java.util.Calendar
import kotlin.random.Random

@Composable
fun WeatherMapScreen(weather: WeatherResponse) {
    val weatherMain = weather.weather.firstOrNull()?.main ?: "Clear"
    val isNight = isNightTime()
    val isRaining = weatherMain.lowercase() in listOf("rain", "drizzle", "thunderstorm")

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMapView(
            lat = weather.coord.lat,
            lng = weather.coord.lon,
            weatherMain = weatherMain,
            isNight = isNight
        )

        // Animación de lluvia sobre el mapa
        if (isRaining) {
            RainOverlay()
        }

        // Card de stats abajo
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNight)
                        Color(0xFF1A1A2E).copy(alpha = 0.9f)
                    else
                        Color.White.copy(alpha = 0.9f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherMapStat(Icons.Rounded.Thermostat, "${weather.main.temp.toInt()}°C", "Temp", isNight)
                    WeatherMapStat(Icons.Rounded.WaterDrop,  "${weather.main.humidity}%",      "Humedad", isNight)
                    WeatherMapStat(Icons.Rounded.Air,        "${weather.wind.speed.toInt()} m/s", "Viento", isNight)
                    WeatherMapStat(Icons.Rounded.Speed,      "${weather.main.pressure} hPa",   "Presión", isNight)
                }
            }
        }

        // Badge día/noche
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (isNight) Icons.Rounded.NightlightRound else Icons.Rounded.WbSunny,
                contentDescription = null,
                tint = if (isNight) Color.White else Grey900,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isNight) "Noche" else "Día",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isNight) Color.White else Grey900
            )
        }
        }
    }

@Composable
fun RainOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "rainDrop"
    )

    val drops = remember {
        List(80) {
            Triple(
                Random.nextFloat(),  // x position
                Random.nextFloat(),  // y offset inicial
                Random.nextFloat() * 0.8f + 0.5f  // speed factor
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drops.forEach { (x, startY, speed) ->
            val y = (startY + rainOffset * speed) % 1f
            val alpha = (0.3f + speed * 0.3f).coerceIn(0f, 0.6f)
            drawLine(
                color = Color(0xFF64B5F6).copy(alpha = alpha),
                start = Offset(size.width * x, size.height * y),
                end = Offset(size.width * x - 2f, size.height * y + 18f),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun WeatherMapStat(icon: ImageVector, value: String, label: String, isNight: Boolean) {
    val textColor = if (isNight) Color.White else Grey900
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(20.dp)
        )
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
        Text(label, fontSize = 11.sp, color = textColor.copy(alpha = 0.6f))
    }
}

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

    // Estilo noche
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

    // Cámara
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))

    // Marcador
    val markerIcon = when (weatherMain.lowercase()) {
        "clear" -> "☀️ Despejado"
        "clouds" -> "☁️ Nublado"
        "rain", "drizzle" -> "🌧️ Lluvia"
        "thunderstorm" -> "⛈️ Tormenta"
        "snow" -> "❄️ Nieve"
        else -> "🌍 Medellín"
    }

    map.addMarker(
        MarkerOptions()
            .position(location)
            .title("Medellín")
            .snippet(markerIcon)
    )

    // Capa lluvia OpenWeather
    if (weatherMain.lowercase() in listOf("rain", "drizzle", "thunderstorm")) {
        val tileProvider = object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): java.net.URL? {
                return try {
                    java.net.URL(
                        "https://tile.openweathermap.org/map/precipitation_new/$zoom/$x/$y.png?appid=697f4c6f14740a27ed2b4ab15573cd82"
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

fun isNightTime(): Boolean {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return hour < 6 || hour >= 19
}