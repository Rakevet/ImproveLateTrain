package com.improve.latetrain

//import com.google.gson.Gson
import android.app.AlertDialog
import android.content.Context
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_add_mins.*


class AddMinsFragment : Fragment() {

    //Locations references
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var geofencingClient: GeofencingClient
    lateinit private var listOfLocations: MutableMap<String, Location>

    //Get reference to firebase database at "Messages"
    private val instance = FirebaseDatabase.getInstance()
    private val waitingPath = instance.getReference("Waiting")
    private val totalWaitingPath = instance.getReference(FirebaseInfo.TOTAL_TIME_PATH)
    private var TAG = "ADD_MIN_FRAG_TAG"
    //Initializing shared preferences
    private val PREF_NAME = "First_Report"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? = inflater.inflate(R.layout.fragment_add_mins, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Reading the stations locations from txt file
        listOfLocations = mutableMapOf()
        val stationNames = arrayListOf<String>()
        activity?.assets?.open("trainstationscoordinates.txt")?.bufferedReader(charset("windows-1255    "))?.useLines {
            it.forEach { line ->
                var stringList: List<String> = line.split(":", ",")
                var geoLocation: Location = Location("").apply {
                    latitude = stringList[1].toDouble()
                    longitude = stringList[2].toDouble()
                }
                listOfLocations[stringList[0]] = geoLocation
                stationNames.add(stringList[0])
            }
        }
        val spinner: Spinner = destinationStationSp
        ArrayAdapter.createFromResource(
            activity!!.applicationContext,
            R.array.stations,
            android.R.layout.simple_spinner_item
        )
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

        addMinBtn.setOnClickListener {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
            geofencingClient = LocationServices.getGeofencingClient(activity!!)
            createLocationRequest()
        }


    }

    fun createLocationRequest() {
        val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences(PREF_NAME, 0)
        var flag = true
//        if (sharedPreferences.contains("addInfo"))
//        {
//            var checkInfo = Gson().fromJson<AddInfoObject>(sharedPreferences.all["addInfo"].toString(),AddInfoObject::class.java)
//            var difference = System.currentTimeMillis()/1000 - checkInfo.timestamp.toLong()
//            if (difference<30)
//            {
//                flag = false
//            }
//        }
        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(activity!!)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            if (ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "you dont have permissions!")
                val permissons = arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
                ActivityCompat.requestPermissions(activity!!, permissons, 123)
            }
            var locationCallback = LocationCallback()
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
            Log.d(TAG, "Requesting locaion updates!")
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null && flag) {
                    Log.d(TAG, "Longitude: ${location.longitude}, Latitude: ${location.latitude}")
                    if (locationSettingsResponse.locationSettingsStates.isGpsUsable) {
                        Log.d(TAG, "the gps is useable!")
                        var flag = false
                        for (i in listOfLocations.keys) {
                            val result = FloatArray(1)
                            Location.distanceBetween(
                                location.latitude, location.longitude,
                                listOfLocations[i]!!.latitude, listOfLocations[i]!!.longitude, result
                            )
                            if (result[0] < 1000f || true) {
                                var minutes = 0
                                if (minLateEt.text.toString() != "") {
                                    minutes = minLateEt.text.toString().toInt()
                                }
                                val tsLong = System.currentTimeMillis() / 1000
                                val timestamp = tsLong
                                var destination = destinationStationSp.selectedItem.toString()
                                val deviceID =
                                    Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)
                                var addInfo = AddInfoObject(minutes, deviceID, i, destination, timestamp)
//                                waitingPath.push().setValue(addInfo)

                                totalWaitingPath.runTransaction(object : Transaction.Handler {
                                    override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {

                                        p0?.message?.let {
                                            Log.d("ERROR_PUSH", it)
                                        }

                                        p2?.let{
                                            Log.d("ERROR_PUSH", p2.childrenCount.toString())
                                        }
                                    }

                                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                        if (mutableData.getValue(Int::class.java) == 0) {
                                            mutableData.value = minutes
                                                return Transaction.success(mutableData);
                                            } else {
                                            val data = mutableData.getValue(Int::class.java)
                                            mutableData.value = data?.plus(minutes)
                                            return Transaction.success(mutableData)
                                        }
                                    }

                                })
                                Log.d(TAG, "Waiting minutes have been added: ${addInfo.minutes}")
                                flag = true
                                break
                            }
                        }
                        if (!flag) {
                            Log.d(TAG, "You are not inside a train station!")
                            AlertDialog.Builder(context)
                                .setTitle("את/ה לא בתחנת רכבת")
                                .setMessage("אתה חייב להיות בתוך תחנת רכבת כדי לדווח על איחור.")
                                .setPositiveButton("הבנתי", null)
                                .show()
                        }
                    }
                } else {
                    Log.d(TAG, "Location is null")
                }
            }
                .addOnFailureListener {
                    Log.d(TAG, "$it")
                }
        }



        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    //TODO We need to show in the app that their loction is off
                    Log.d("TESTS", "Showing request for changing settings")
                    exception.startResolutionForResult(activity!!, 0x1)

                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            AddMinsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
