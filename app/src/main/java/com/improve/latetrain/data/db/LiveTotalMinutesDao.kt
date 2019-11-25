package com.improve.latetrain.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LiveTotalMinutesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveTotalMinutes(liveTotalMinutes: LiveTotalMinutes)

    @Query("SELECT * FROM minutes_table WHERE id=1")
    fun getLiveTotalMinutes(): LiveData<LiveTotalMinutes>
}