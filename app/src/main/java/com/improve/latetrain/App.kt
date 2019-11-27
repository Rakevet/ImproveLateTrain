package com.improve.latetrain

import android.app.Application
import com.improve.latetrain.data.DrawerRepository
import com.improve.latetrain.data.db.AppDatabase
import com.improve.latetrain.viewmodel.DrawerViewModel
import com.improve.latetrain.viewmodel.DrawerViewModelFactory
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin

class App: Application() {

    private val drawerViewModelModules = module {
        viewModel {
            DrawerViewModel(
                get(),
                Application()
            )
        }
        single {
            DrawerViewModelFactory(
                get(),
                Application()
            )
        }
        single {
            DrawerRepository(
                AppDatabase.getDatabase(applicationContext).liveTotalMinutesDao()
            )
        }
    }

    private val moduleList = listOf(drawerViewModelModules)

    override fun onCreate() {
        super.onCreate()
        startKoin(moduleList)
    }
}