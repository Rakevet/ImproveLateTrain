package com.improve.latetrain

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DrawerViewModelFactory(private val repository: DrawerRepository, private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DrawerViewModel(repository, application) as T
    }
}