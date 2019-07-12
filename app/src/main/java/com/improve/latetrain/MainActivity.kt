package com.improve.latetrain

import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var textMessage: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                textMessage.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                textMessage.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                textMessage.setText(R.string.title_notifications)
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

        //Get reference to firebase database at "Messages"
        val instance = FirebaseDatabase.getInstance()
        val waitingPath = instance.getReference("Waiting")

        //TODO enter those two line into an OnClickListener and change AddInfoObject parameters
        var addInfo = AddInfoObject(15,"testUID", "station", "destination", "129371923")
        waitingPath.push().setValue(addInfo)


        var sum = 0
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if (dataSnapshot.exists())
                {
                    dataSnapshot.children.forEach {child ->
                        var infoChild = child.getValue(AddInfoObject::class.java)
                        Log.d(TAG, infoChild?.minutes.toString())
                        if(infoChild!=null)
                            sum += infoChild.minutes
                    }
                    //TODO enter the sum into the Live parameter in the fragment
                    //here!
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
        var permissons = arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                 android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this,permissons,123)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        //Reading the stations locations from txt file
        var list: MutableMap<String, Location> = mutableMapOf()
        applicationContext.assets.open("trainstationscoordinates.txt").bufferedReader().useLines {
            it.forEach { line ->
                var stringList: List<String>  = line.split(":",",")
                var geoLocation: Location = Location("").apply {
                    latitude = stringList[1].toDouble()
                    longitude = stringList[2].toDouble()
                }
                list[stringList[0]] =  geoLocation
            }
        }

    }
    fun createLocationRequest(){
        val locationRequest = LocationRequest.create().apply {
            interval = 10
            fastestInterval = 10
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            {
                var permissons = arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)
                Log.d("TESTS", "you dont have permissions!")
                ActivityCompat.requestPermissions(this,permissons,123)
            }
            var locationCallback = LocationCallback()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
            Log.d("TESTS", "send Location!")
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Log.d("TESTS", "Longitude: ${location.longitude}, Latitude: ${location.latitude}")
                    }
                    else
                    {
                        Log.d("TESTS", "Location is null")
                    }
                }
                .addOnFailureListener {
                    Log.d("TESTS", "$it")
                }
            if (locationSettingsResponse.locationSettingsStates.isGpsUsable) {
                Log.d("TESTS", "the gps is useable!")
            }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    //TODO We need to show in the app that their loction is off
                    Log.d("TESTS", "Showing request for changing settings")
                    exception.startResolutionForResult(this@MainActivity, 0x1)

                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

    }


}
