package com.yulian.citypulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yulian.citypulse.ui.screens.DashboardScreen
import com.yulian.citypulse.ui.theme.CityPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CityPulseTheme {
                DashboardScreen()
            }
        }
    }
}