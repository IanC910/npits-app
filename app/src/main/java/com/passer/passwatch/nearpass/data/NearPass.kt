package com.passer.passwatch.nearpass.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.passer.passwatch.ride.data.Ride

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Ride::class,
            parentColumns = ["id"],
            childColumns = ["rideId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
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
