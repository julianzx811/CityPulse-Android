package com.yulian.citypulse.ui.utils

import java.util.Calendar

fun isNightTime(): Boolean {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return hour < 6 || hour >= 19
}