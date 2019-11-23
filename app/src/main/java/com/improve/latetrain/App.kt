package com.improve.latetrain

import android.app.Application
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin

class App: Application() {

    private val drawerViewModelModules = module {
        viewModel { DrawerViewModel(get()) }
        single { DrawerViewModelFactory(get()) }
        single { DrawerRepository() }
    }

    private val moduleList = listOf(drawerViewModelModules)

    override fun onCreate() {
        super.onCreate()
        startKoin(moduleList)
    }
}