package com.passer.passwatch.core.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatTime(t: Long?): String {
    if(t == null) {
        return ""
    }

    val hours = t / 3600
    val minutes = (t % 3600) / 60
    val seconds = t % 60

    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

fun epochToUTC(epochSeconds: Long?): String {
    if(epochSeconds == null) {
        return ""
    }

    val instant = Instant.fromEpochSeconds(epochSeconds)
    val utcDateTime = instant.toLocalDateTime(TimeZone.UTC)
    return utcDateTime.toString()
}