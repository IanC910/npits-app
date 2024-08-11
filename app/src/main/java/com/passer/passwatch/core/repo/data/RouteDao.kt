package com.passer.passwatch.core.repo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Insert
    suspend fun insertRoute(route: Route) : Long

    @Delete
    suspend fun deleteRoute(route: Route)

    @Query("SELECT * FROM route WHERE rideId = :rideId ORDER BY id ASC")
    fun getRoutesForRide(rideId: Int): Flow<List<Route>>

    @Query("DELETE FROM route WHERE rideId = :rideId")
    suspend fun deleteRoutesForRide(rideId: Int)
}
