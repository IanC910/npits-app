package com.passer.passwatch.model.nearpass

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.passer.passwatch.model.ride.Ride

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
    val latitude: Double,
    val longitude: Double,
    val distance: Double,
    val speed: Double,

    val rideId: Int? = null,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)
