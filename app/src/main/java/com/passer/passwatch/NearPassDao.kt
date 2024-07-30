package com.passer.passwatch

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
    fun getContactsOrderedById() : Flow<List<NearPass>>
}