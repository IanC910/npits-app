package com.passer.passwatch.ride.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Ride(
    var startTime: Long?,
    var endTime: Long?,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
)