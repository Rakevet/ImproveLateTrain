package com.improve.latetrain.fragments

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
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.*
import com.improve.latetrain.AnalyticsInfo.sendAnalytics
import com.improve.latetrain.BuildConfig
import com.improve.latetrain.FirebaseInfo
import com.improve.latetrain.LocationCheck
import com.improve.latetrain.R
import com.improve.latetrain.adapters.SpinnerAdapter
import kotlinx.android.synthetic.main.fragment_add_mins.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class AddMinsFragment : Fragment() {
    private val TAG = "ADD_MIN_FRAG_TAG"
    private val LAST_CLICK = "LAST_CLICK"
    private val REQUEST_CHECK_SETTINGS = 1
    private val MY_PERMISSIONS_REQUEST_LOCATION = 2
    private val IS_PERMISSION_REQUEST_GRANTED = "IS_PERMISSION_REQUEST_GRANTED"
    lateinit var stationsList: MutableMap<String, Location>
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val instance = FirebaseDatabase.getInstance()
    private val totalWaitingPath = instance.getReference(FirebaseInfo.TOTAL_TIME_PATH)
    private val totalDaysPath = instance.getReference(FirebaseInfo.TOTAL_DAYS)
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var lastTime: Long = 0
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? = inflater.inflate(R.layout.fragment_add_mins, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            sharedPreferences = it.getSharedPreferences("com.improve.latetrain", Context.MODE_PRIVATE)
            firebaseAnalytics = FirebaseAnalytics.getInstance(it.baseContext)
        }
        lastTime = sharedPreferences.getLong(LAST_CLICK, 0)

        (animation_layout as LottieAnimationView).playAnimation()

        if(lastTime + 1800 > System.currentTimeMillis() / 1000 && !BuildConfig.DEBUG){
            addMinBtn.visibility = View.GONE
            success_addMinBtn.visibility = View.VISIBLE
            (animation_layout as LottieAnimationView).cancelAnimation()
            animation_layout.visibility = View.INVISIBLE
            val minutesPassed = 30 - (((System.currentTimeMillis() / 1000) - lastTime) / 60)
            Toast.makeText(context, getString(R.string.remaining_time_add_mins, minutesPassed), Toast.LENGTH_LONG).show()
        }

        addPermission.setOnClickListener {
            useLocationPermission()
        }

        addMinBtn.setOnClickListener {
            if (!isLocationOn()){
                setLocationRequests()
                return@setOnClickListener
            }
            var minutes = 0
            if (minLateEt.selectedItem.toString().isNotBlank()) {
                minutes = minLateEt.selectedItem.toString().toInt()
            }
            val isDestinationSelected: Boolean =
                destinationStationSp.selectedItem.toString() != resources.getStringArray(R.array.stations)[0]
            val isInStation: Boolean =
                current_station_location_fam.text.toString() != getString(R.string.not_in_train_station_addmins)
            val isNotSame = destinationStationSp.selectedItem.toString() != current_station_location_fam.text.toString()

            if (!isInStation) {
                displayDismissedDialog(
                    getString(R.string.your_not_at_station),
                    getString(R.string.complete_info_for_update),
                    getString(R.string.got_it_addmins)
                )
                return@setOnClickListener
            }

            if (minutes > 0 && isDestinationSelected && isNotSame) {
                addMinBtn.visibility = View.GONE
                waiting_addMinBtn.visibility = View.VISIBLE
                val currentDay = SimpleDateFormat("dd", Locale.getDefault()).format(Date()).toInt()
                val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date()).toInt()
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()).toInt()

                totalWaitingPath.runTransaction(object : Transaction.Handler {
                    override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                        totalDaysPath.child(currentYear.toString()).child(currentMonth.toString())
                            .child(currentDay.toString()).child("minutes")
                            .runTransaction(object : Transaction.Handler {
                                override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                                    waiting_addMinBtn.visibility = View.GONE
                                    success_addMinBtn.visibility = View.VISIBLE
                                    val animation = AlphaAnimation(1f, 0f)
                                    animation.duration = 500
                                    animation.interpolator = LinearInterpolator()
                                    animation.repeatCount = 1
                                    animation.repeatMode = Animation.REVERSE
                                    success_addMinBtn.startAnimation(animation)
                                    (animation_layout as LottieAnimationView).cancelAnimation()
                                    animation_layout.visibility = View.INVISIBLE
                                    context?.let{
                                        sendAnalytics("addMinBtn", arrayListOf(Pair("", "")), it)
                                    }
                                    Toast.makeText(context, getString(R.string.thanks_he), Toast.LENGTH_LONG).show()
                                    Handler().postDelayed({
                                        Toast.makeText(context, getString(R.string.waiting_message_addmins), Toast.LENGTH_LONG).show()
                                    }, 2000)
                                }
                                override fun doTransaction(p0: MutableData): Transaction.Result {
                                    if (p0.getValue(Int::class.java) == null) {
                                        p0.value = minutes
                                        totalDaysPath.child(currentYear.toString()).child(currentMonth.toString())
                                            .child(currentDay.toString()).child("date").setValue(currentDay)
                                        return Transaction.success(p0)
                                    }
                                    val dayData = p0.getValue(Int::class.java)
                                    p0.value = dayData?.plus(minutes)
                                    return Transaction.success(p0)
                                }
                            })
                    }

                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        sharedPreferences.edit()?.putLong(LAST_CLICK, System.currentTimeMillis() / 1000)?.apply()
                        if (mutableData.getValue(Int::class.java) == null) {
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

    private fun displayDismissedDialog(title: String, message: String, posBtnText: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(posBtnText) { _, _ -> }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        if (!sharedPreferences.getBoolean(IS_PERMISSION_REQUEST_GRANTED, false))
            useLocationPermission()
        else
            setLocationRequests()
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            if (ContextCompat.checkSelfPermission(it.baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun useLocationPermission() {
        activity?.let {
            if (ContextCompat.checkSelfPermission(it.baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            } else
                setLocationRequests()
        }
    }

    private fun isLocationOn(): Boolean {
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var isGpsEnabled = false
        try {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        return isGpsEnabled
    }

    @SuppressLint("MissingPermission")
    fun setLocationRequests() {
        LocationCheck.isLocationRequestSet = true
        locationRequest = LocationRequest.create().apply {
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
                    if (!isLocationOn())
                        startIntentSenderForResult(
                            exception.resolution.intentSender, REQUEST_CHECK_SETTINGS,
                            null, 0, 0, 0, null)
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