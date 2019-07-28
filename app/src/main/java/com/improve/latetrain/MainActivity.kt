package com.improve.latetrain

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val instance = FirebaseDatabase.getInstance()
    private val waitingPath = instance.getReference("Waiting")
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
        val TAG = "TESTINGMAIN"
        //Bottom navigation view setup
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        navView.selectedItemId = R.id.navigation_add_mins

        var sum = 0
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists())
                {
                    sum = 0
                    dataSnapshot.children.forEach {child ->
                        var infoChild = child.getValue(AddInfoObject::class.java)
                        if(infoChild!=null)
                            sum += infoChild.minutes
                    }
                    //sum.toString()
                    live_minutes.text = sum.toString()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        waitingPath.addValueEventListener(postListener)



        //Locaion setup

    }
}
