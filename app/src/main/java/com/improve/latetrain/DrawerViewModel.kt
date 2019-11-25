package com.improve.latetrain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.improve.latetrain.data.db.LiveTotalMinutes

class DrawerViewModel(private val repository: DrawerRepository, application: Application):
    AndroidViewModel(application) {

    private val scope = viewModelScope

    val totalMinutes: LiveData<LiveTotalMinutes> = repository.totalMinutes

    fun startListeningForTotalWaitingMinutes(){
        repository.startListeningForTotalWaitingMinutes(scope)
    }

    fun stopListeningForTotalWaitingMinutes(){
        repository.stopListeningForTotalWaitingMinutes()
    }
}