package com.yulian.citypulse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import com.yulian.citypulse.ui.theme.*
import com.yulian.citypulse.ui.components.map.WeatherMapStat
import com.yulian.citypulse.ui.components.map.RainOverlay
import com.yulian.citypulse.ui.components.map.GoogleMapView
import com.yulian.citypulse.data.model.WeatherResponse
import com.yulian.citypulse.ui.utils.isNightTime

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
                        NightBackground.copy(alpha = 0.9f)
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