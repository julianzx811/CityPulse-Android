package com.yulian.citypulse.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Warning
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yulian.citypulse.ui.components.WeatherAnimation
import com.yulian.citypulse.ui.theme.*
import com.yulian.citypulse.viewmodel.WeatherUiState
import com.yulian.citypulse.viewmodel.WeatherViewModel
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(viewModel: WeatherViewModel = viewModel()) {
    val weatherState by viewModel.weatherState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getWeather("Medellin")
    }

    val gradientColors = when (weatherState) {
        is WeatherUiState.Success -> {
            val w = (weatherState as WeatherUiState.Success).weather.weather.firstOrNull()?.main ?: ""
            when (w.lowercase()) {
                "clear"                        -> WeatherClear
                "clouds"                       -> WeatherCloudy
                "rain", "drizzle"              -> WeatherRain
                "thunderstorm"                 -> WeatherStorm
                "snow"                         -> WeatherSnow
                else                           -> WeatherClear
            }
        }
        else -> WeatherClear
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (showSearch) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Buscar ciudad...", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = {
                                if (searchText.isNotBlank()) {
                                    viewModel.searchCity(searchText.trim())
                                    showSearch = false
                                    searchText = ""
                                }
                            }
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { showSearch = false; searchText = "" }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.LocationCity,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "CityPulse",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { showSearch = true }) {
                        Icon(Icons.Rounded.Search, contentDescription = "Buscar", tint = Color.White)
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Rounded.WbSunny, contentDescription = "Clima") },
                    text = { Text("Clima") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Rounded.Map, contentDescription = "Mapa") },
                    text = { Text("Mapa") }
                )
            }

            when (weatherState) {
                is WeatherUiState.Loading -> LoadingContent()
                is WeatherUiState.Error   -> ErrorContent((weatherState as WeatherUiState.Error).message)
                is WeatherUiState.Success -> {
                    val success = weatherState as WeatherUiState.Success
                    if (selectedTab == 0) WeatherContent(success)
                    else WeatherMapScreen(success.weather)
                }
            }
        }
    }
}

@Composable
fun LoadingContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.Public,
                contentDescription = null,
                tint = Color.White.copy(alpha = alpha),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Cargando CityPulse...",
                color = Color.White.copy(alpha = alpha),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorContent(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Error al cargar el clima", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(message, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }
    }
}

@Composable
fun WeatherContent(state: WeatherUiState.Success) {
    val weather     = state.weather
    val weatherMain = weather.weather.firstOrNull()?.main ?: "Clear"
    val weatherDesc = weather.weather.firstOrNull()?.description ?: ""
    val temp        = weather.main.temp.roundToInt()
    val feelsLike   = weather.main.feels_like.roundToInt()

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
        label = "float"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "${weather.name}, ${weather.sys.country}",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = weatherDesc.replaceFirstChar { it.uppercase() },
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
        Spacer(Modifier.height(24.dp))
        WeatherAnimation(
            weatherMain = weatherMain,
            modifier = Modifier
                .size(200.dp)
                .offset(y = floatOffset.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(text = "$temp°", color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.Thin)
        Text(text = "Sensación $feelsLike°", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WeatherCard(Modifier.weight(1f), Icons.Rounded.WaterDrop,  "Humedad", "${weather.main.humidity}%")
            WeatherCard(Modifier.weight(1f), Icons.Rounded.Air,         "Viento",  "${weather.wind.speed.roundToInt()} m/s")
            WeatherCard(Modifier.weight(1f), Icons.Rounded.Thermostat,  "Presión", "${weather.main.pressure} hPa")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card")
    val cardFloat by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2000 + (label.length * 100)), RepeatMode.Reverse),
        label = "cardFloat"
    )
    Box(
        modifier = modifier
            .offset(y = cardFloat.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}