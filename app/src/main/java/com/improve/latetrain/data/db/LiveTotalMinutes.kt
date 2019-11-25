package com.improve.latetrain.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "minutes_table")
class LiveTotalMinutes(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "liveTotalMinutes") val liveTotalMinutes: Long
)