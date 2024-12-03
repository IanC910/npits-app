package com.passer.passwatch.nearpass.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NearPassDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNearPass(nearPass: NearPass)

    @Delete
    suspend fun deleteNearPass(nearPass: NearPass)

    @Query("SELECT * FROM nearpass ORDER BY id ASC")
    fun getNearPassesOrderedById() : Flow<List<NearPass>>

    @Query("SELECT * FROM nearpass WHERE rideId = :rideId ORDER BY id ASC")
    fun getNearPassesForRide(rideId : Int) : Flow<List<NearPass>>

    @Query("SELECT * FROM nearpass ORDER BY id ASC")
    suspend fun allNearPasses(): List<NearPass>

    @Query("DELETE FROM nearpass")
    suspend fun deleteAllNearPasses()
}