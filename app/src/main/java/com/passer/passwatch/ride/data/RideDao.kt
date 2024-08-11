package com.passer.passwatch.ride.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: Ride) : Long

    @Delete
    suspend fun deleteRide(ride: Ride)

    @Query("SELECT * FROM ride ORDER BY id ASC")
    fun getRidesOrderedById() : Flow<List<Ride>>

    @Query("UPDATE ride SET endTime = :endTime WHERE id = :rideId")
    suspend fun updateRideEndTime(rideId: Int, endTime: Long)
}