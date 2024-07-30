package com.passer.passwatch

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class NearPass(
    val latitude: Double,
    val longitude: Double,
    val distance: Double,
    val speed: Double,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)
