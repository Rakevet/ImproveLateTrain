package com.improve.latetrain

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_add_mins.*
import kotlin.math.absoluteValue

class AddMinsFragment : Fragment() {
    private val TAG = "ADD_MIN_FRAG_TAG"
    private val LAST_CLICK = "LAST_CLICK"
    private val REQUEST_CHECK_SETTINGS = 0x1
    private val MY_PERMISSIONS_REQUEST_LOCATION = 0x2
    private val IS_PERMISSION_REQUEST_GRANTED = "IS_PERMISSION_REQUEST_GRANTED"
    lateinit var stationsList: MutableMap<String, Location>
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val instance = FirebaseDatabase.getInstance()
    private val totalWaitingPath = instance.getReference(FirebaseInfo.TOTAL_TIME_PATH)
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? = inflater.inflate(R.layout.fragment_add_mins, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (animation_layout as LottieAnimationView).playAnimation()

        activity?.let {
            sharedPreferences = it.getSharedPreferences("com.improve.latetrain", Context.MODE_PRIVATE)
        }

        addPermission.setOnClickListener {
            useLocationPermission()
        }

        addMinBtn.setOnClickListener {
            val lastTime = sharedPreferences.getLong(LAST_CLICK, 0)
            Log.d(TAG, lastTime.toString())
            var is30MinPass = true
            if (lastTime + 1800 > System.currentTimeMillis() / 1000) {
                is30MinPass = false
                val builder = AlertDialog.Builder(context)
                builder.setTitle(getString(R.string.wait_addmins))
                builder.setMessage(getString(R.string.once_in_30_addmins))
                builder.setPositiveButton(getString(R.string.got_it_addmins)) { _, _ -> }
                val dialog: AlertDialog = builder.create()
                dialog.show()
                return@setOnClickListener
            }

            var minutes = 0
            if (minLateEt.selectedItem.toString().isNotBlank()) {
                minutes = minLateEt.selectedItem.toString().toInt()
            }
            val isDestinationSelected: Boolean =
                destinationStationSp.selectedItem.toString() != destinationStationSp[0].toString()
            val isInStation: Boolean = current_station_location_fam.text.toString()!=getString(R.string.not_in_train_station_addmins)
            val isNotSame = destinationStationSp.selectedItem.toString() != current_station_location_fam.text.toString()

            if (!isInStation) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("אתה לא בתחנת רכבת!")
                builder.setMessage("חייבים להיות בתוך תחנת רכבת בשביל לדווח!")
                builder.setPositiveButton(getString(R.string.got_it_addmins)) { _, _ -> }
                val dialog: AlertDialog = builder.create()
                dialog.show()
                return@setOnClickListener
            }

            if (minutes > 0 && isDestinationSelected && isInStation && is30MinPass && isNotSame) {
                totalWaitingPath.runTransaction(object : Transaction.Handler {
                    override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {}

                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        sharedPreferences.edit()?.putLong(LAST_CLICK, System.currentTimeMillis() / 1000)?.apply()
                        if (mutableData.getValue(Int::class.java) == 0) {
                            mutableData.value = minutes
                            return Transaction.success(mutableData)
                        }
                        val data = mutableData.getValue(Int::class.java)
                        mutableData.value = data?.plus(minutes)
                        return Transaction.success(mutableData)
                    }
                })
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.please_fill_requiered_information_addmins),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        //Spinners
        val adapterLocations = SpinnerAdapter(
            context ?: return,
            R.layout.spinner_dropdown_layout,
            resources.getStringArray(R.array.stations).toList()
        )
        destinationStationSp.adapter = adapterLocations

        val minutesList = arrayListOf<String>().also {
            for (i in 0..60)
                it.add("$i")
        }

        val adapterMinutes = SpinnerAdapter(
            context ?: return,
            R.layout.spinner_dropdown_layout,
            minutesList
        )
        minLateEt.adapter = adapterMinutes
        try {
            val popup = Spinner::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val popupWindow = popup.get(minLateEt) as android.widget.ListPopupWindow
            popupWindow.height = 500
        } catch (e: NoClassDefFoundError) {
        } catch (e: ClassCastException) {
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        }

        stationsList = mutableMapOf()
        val textStations = activity?.assets?.open("trainstationscoordinates.txt")?.bufferedReader()
        textStations?.forEachLine { line ->
            val lineList = line.split(":")
            val longiNlatti = lineList[1].split(",")
            val location = Location(LocationManager.GPS_PROVIDER)
            location.longitude = longiNlatti[1].toDouble()
            location.latitude = longiNlatti[0].toDouble()
            stationsList.put(lineList[0], location)
        }

        activity?.let {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(it.baseContext)
        }
    }

    private fun useLocationPermission() {
        activity?.let {
            if (ContextCompat.checkSelfPermission(it.baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION)
            } else
                setLocationRequests()
        }
    }

    override fun onResume() {
        super.onResume()
            if (sharedPreferences.getBoolean(IS_PERMISSION_REQUEST_GRANTED, true))
                useLocationPermission()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        setLocationRequests()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    fun setLocationRequests() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(context ?: return)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {}
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null) {
                    val lastLocationRecieved = locationResult.lastLocation
                    val result = FloatArray(1)
                    var isInStation = false
                    for (i in stationsList.keys) {
                        Location.distanceBetween(
                            lastLocationRecieved.latitude, lastLocationRecieved.longitude,
                            stationsList[i]?.latitude ?: 0.0, stationsList[i]?.longitude ?: 0.0, result
                        )
                        if (result[0].absoluteValue < 500) {
                            current_station_location_fam?.text = i
                            isInStation = true
                            fusedLocationClient.removeLocationUpdates(this)
                            break
                        }
                    }
                    if (!isInStation) {
                        current_station_location_fam?.text = getString(R.string.not_in_train_station_addmins)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "5")
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addMinBtn.visibility = View.VISIBLE
                    addPermission.visibility = View.GONE
                    setLocationRequests()
                    sharedPreferences.edit().putBoolean(IS_PERMISSION_REQUEST_GRANTED, true).apply()
                } else {
                    addMinBtn.visibility = View.GONE
                    addPermission.visibility = View.VISIBLE
                    sharedPreferences.edit().putBoolean(IS_PERMISSION_REQUEST_GRANTED, false).apply()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddMinsFragment()
    }
}