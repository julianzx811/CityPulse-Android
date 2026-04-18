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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yulian.citypulse.ui.components.WeatherAnimation
import com.yulian.citypulse.ui.theme.*
import com.yulian.citypulse.viewmodel.WeatherUiState
import com.yulian.citypulse.viewmodel.WeatherViewModel
import kotlin.math.roundToInt
import com.yulian.citypulse.ui.components.common.LoadingContent
import com.yulian.citypulse.ui.components.common.ErrorContent
import com.yulian.citypulse.ui.components.dashboard.SearchBar
import com.yulian.citypulse.ui.components.dashboard.WeatherCard

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
            SearchBar(
                showSearch = showSearch,
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onSearch = { viewModel.searchCity(it); showSearch = false; searchText = "" },
                onOpenSearch = { showSearch = true },
                onCloseSearch = { showSearch = false; searchText = "" }
            )

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