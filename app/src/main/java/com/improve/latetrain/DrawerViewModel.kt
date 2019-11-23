package com.improve.latetrain

import androidx.lifecycle.ViewModel

class DrawerViewModel(private val repository: DrawerRepository): ViewModel() {

    fun test(): String{
        return "Test is working!"
    }
}