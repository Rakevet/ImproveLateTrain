package com.improve.latetrain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DrawerViewModelFactory(private val repository: DrawerRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DrawerViewModel(repository) as T
    }
}