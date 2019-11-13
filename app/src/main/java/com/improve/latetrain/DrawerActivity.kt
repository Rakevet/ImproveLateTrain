package com.improve.latetrain

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.improve.latetrain.fragments.AddMinsFragment
import com.improve.latetrain.fragments.ChatFragment
import com.improve.latetrain.fragments.HistoryFragment
import com.improve.latetrain.fragments.PicturesGalleryFragment
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.app_bar_drawer.*
import kotlinx.android.synthetic.main.content_drawer.*
import kotlinx.android.synthetic.main.live_bar_layout.*
import java.math.RoundingMode
import java.text.DecimalFormat

class DrawerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var shareActionProvider: ShareActionProvider? = null
    private lateinit var bottomNavView: BottomNavigationView
    private var localFragmentManager = supportFragmentManager
    private lateinit var sendIntent: Intent
    private var isSwithChecked = true
    private var lastMinutes = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)

        getMinutes(live_minutes)
        setSwitch(minutes_nis_switch, live_minutes, textlivebar_tv)

        sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, baseContext?.resources?.getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT,
                baseContext?.resources?.getString(R.string.share_app_url_drawer) + BuildConfig.APPLICATION_ID)
        }

        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        bottomNavView = bottom_nav_view
        bottomNavView.setOnNavigationItemSelectedListener(onBottomNavigationItemSelectedListener)
        bottomNavView.selectedItemId = R.id.navigation_add_mins
    }

    fun setSwitch(switch: Switch, minutes: TextView, text_bar: TextView){
        switch.isChecked = isSwithChecked
        switch.setOnClickListener {
            changeLiveContent(switch, minutes, text_bar)
        }
    }

    private fun changeLiveContent(switch: Switch, minutes: TextView, text_bar: TextView){
        if(switch.isChecked){
            isSwithChecked = true
            minutes.text = lastMinutes
            text_bar.text = getString(R.string.train_waiting_time_content)
        }
        else{
            isSwithChecked = false
            val number = lastMinutes.toDouble()*1.016
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.CEILING
            minutes.text = df.format(number).toString()
            text_bar.text = getString(R.string.train_waiting_money_content)
        }
    }

    fun getMinutes(minutes: TextView){
        minutes.text = getString(R.string.no_internet_connection_drawer)
        val instance = FirebaseDatabase.getInstance()
        val totalMinutesLate = instance.getReference(FirebaseInfo.TOTAL_TIME_PATH)
        totalMinutesLate.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                lastMinutes = p0.value.toString()
                if(isSwithChecked)
                    minutes.text = lastMinutes
                else{
                    val number = lastMinutes.toDouble()*1.016
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.CEILING
                    minutes.text = df.format(number).toDouble().toString()
                }
            }
        })
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.drawer, menu)
        shareActionProvider = ShareActionProvider(this)
        shareActionProvider?.setShareIntent(
            Intent.createChooser(
                sendIntent,
                getString(R.string.share_drawer)
            )
        )
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else
            drawer_layout.openDrawer(GravityCompat.START)
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent  = when (item.itemId) {
            R.id.nav_home -> Intent(this, DrawerActivity::class.java)
            R.id.nav_about_us -> Intent(this, AboutUsActivity::class.java)
            R.id.nav_write_to_us -> Intent(this, WriteUsActivity::class.java)
            else -> null
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        if(intent!=null)
            startActivity(intent)
        else
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_drawer)))
        return true
    }

    private val onBottomNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            minutes_nis_switch.isChecked = isSwithChecked
            changeLiveContent(minutes_nis_switch, live_minutes, textlivebar_tv)
            live_bar.visibility = View.VISIBLE
            val fragment = when (item.itemId) {
                R.id.navigation_add_mins -> AddMinsFragment.newInstance()
                R.id.navigation_gallery -> {
                    live_bar.visibility = View.GONE
                    PicturesGalleryFragment.newInstance()
                }
                R.id.navigation_complaints -> ChatFragment.newInstance()
                R.id.navigation_history -> HistoryFragment.newInstance()
                else -> AddMinsFragment.newInstance()
            }
            localFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit()
            true
        }
}