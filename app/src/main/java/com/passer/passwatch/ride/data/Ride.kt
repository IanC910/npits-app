package com.passer.passwatch.ride.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Ride(
    val startTime: Long?,
    val endTime: Long?,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)