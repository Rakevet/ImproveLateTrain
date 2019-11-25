package com.improve.latetrain.data.firebase

interface IFirebaseObserver {
    suspend fun totalMinutesChanged(minutes: Long)
}