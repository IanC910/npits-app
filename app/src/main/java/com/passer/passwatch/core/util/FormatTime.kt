package com.passer.passwatch.core.util

fun formatTime(t: Long): String {
    val hours = t / 3600
    val minutes = (t % 3600) / 60
    val seconds = t % 60

    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}