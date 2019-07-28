package com.improve.latetrain

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val TAG = "MAIN_ACTIVITY_TAG"


    private val instance = FirebaseDatabase.getInstance()
    private val totalMinutesLate = instance.getReference(FirebaseInfo.TOTAL_TIME_PATH)

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val fragment = when (item.itemId) {
            R.id.navigation_add_mins -> AddMinsFragment.newInstance()
            R.id.navigation_history -> HistoryFragment.newInstance()
            R.id.navigation_complaints -> ComplaintsFragment.newInstance()
            else -> ComplaintsFragment()
        }
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        //Bottom navigation view setup
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        navView.selectedItemId = R.id.navigation_add_mins

        totalMinutesLate.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                live_minutes.text = String.format(getString(R.string.live_info_display_text), p0.value)
            }
        })
    }
}
