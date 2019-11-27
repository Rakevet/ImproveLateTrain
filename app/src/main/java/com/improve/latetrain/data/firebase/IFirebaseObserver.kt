package com.improve.latetrain.data.firebase

import com.google.firebase.firestore.QuerySnapshot
import com.improve.latetrain.data.entities.Message

interface IFirebaseObserver {

    suspend fun totalMinutesChanged(minutes: Long)
    fun dailyMinutesChildAdded(pair: Pair<Int, String>)
    fun uploadMinutesComplete(message: String)
    fun messageChildAdded(message: Message)
    fun getImagesSuccess(result: QuerySnapshot)
    fun uploadImageComplete(message: String)
}