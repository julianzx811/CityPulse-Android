package com.yulian.citypulse.ui.components.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yulian.citypulse.ui.theme.Grey900

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