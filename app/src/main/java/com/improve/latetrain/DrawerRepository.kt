package com.improve.latetrain

import androidx.lifecycle.LiveData
import com.improve.latetrain.data.db.LiveTotalMinutes
import com.improve.latetrain.data.db.LiveTotalMinutesDao
import com.improve.latetrain.data.firebase.FirebaseConnection
import com.improve.latetrain.data.firebase.IFirebaseObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DrawerRepository(
    private val liveTotalMinutesDao: LiveTotalMinutesDao,
    private val firebaseConnection: FirebaseConnection
) : IFirebaseObserver {

    val totalMinutes: LiveData<LiveTotalMinutes> = liveTotalMinutesDao.getLiveTotalMinutes()

    init {
        firebaseConnection.addObserver(this)
    }

    override suspend fun totalMinutesChanged(minutes: Long) {
        insertTotalMinutes(minutes)
    }

    fun startListeningForTotalWaitingMinutes(scope: CoroutineScope) {
        firebaseConnection.getTotalWaitingMinutes(scope)
    }

    fun stopListeningForTotalWaitingMinutes() {
        firebaseConnection.removeTotalWaitingMinutesListener()
    }

    private suspend fun insertTotalMinutes(minutes: Long) {
        val liveTotalMinutes = LiveTotalMinutes(1, minutes)
        liveTotalMinutesDao.insertLiveTotalMinutes(liveTotalMinutes)
    }
}