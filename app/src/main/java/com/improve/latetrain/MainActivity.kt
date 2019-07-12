package com.improve.latetrain

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import com.improve.latetrain.R

class MainActivity : AppCompatActivity() {

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_add_mins -> {
                supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, AddMinsFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_history -> {
                supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, HistoryFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_complaints -> {
                supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, ComplaintsFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        //Get reference to firebase database at "Messages"
        val instance = FirebaseDatabase.getInstance()
        //val ref = instance.getReference("Waiting")


        //ref.push().setValue()
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }
}
