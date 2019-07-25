package com.improve.latetrain

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import com.improve.latetrain.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment =  when (item.itemId) {
            R.id.navigation_add_mins -> {
                AddMinsFragment()
            }
            R.id.navigation_history -> {
                HistoryFragment()
            }
            R.id.navigation_complaints -> {
                ComplaintsFragment()
            }
            else->AddMinsFragment()
        }
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit()
        true
    }
}
