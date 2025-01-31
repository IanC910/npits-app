package com.passer.passwatch.core.repo.data

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

data class Route(
    val latitude: Double,
    val longitude: Double,
    val speed: Double?,
    val time: Long,

    val rideId: Int? = null,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)
