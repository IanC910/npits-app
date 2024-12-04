package com.passer.passwatch.nearpass.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NearPass(
    var latitude: Double?,
    var longitude: Double?,
    var distance: Double?,
    var speed: Double?,
    var time: Long?,

    var rideId: Int? = null,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
)
