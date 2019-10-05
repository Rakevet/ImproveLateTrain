package com.improve.latetrain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ShareActionProvider
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.improve.latetrain.fragments.*
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.content_drawer.*

class DrawerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG = "DrawerActivityTag"
    private var shareActionProvider: ShareActionProvider? = null
    private lateinit var bottomNavView: BottomNavigationView
    private var localFragmentManager = supportFragmentManager
    private lateinit var sendIntent: Intent
    private var isLocationRequestSet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)

        //push check

        sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, baseContext?.resources?.getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, baseContext?.resources?.getString(R.string.share_app_url_drawer) + BuildConfig.APPLICATION_ID)
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        bottomNavView = bottom_nav_view
        bottomNavView.setOnNavigationItemSelectedListener(onBottomNavigationItemSelectedListener)
        bottomNavView.selectedItemId = R.id.navigation_add_mins

        live_minutes.text = getString(R.string.no_internet_connection_drawer)
        val instance = FirebaseDatabase.getInstance()
        val totalMinutesLate = instance.getReference(FirebaseInfo.TOTAL_TIME_PATH)
        totalMinutesLate.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                live_minutes.text = p0.value.toString()
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
        shareActionProvider?.setShareIntent(Intent.createChooser(sendIntent, getString(R.string.share_drawer)))
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_share -> {
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_drawer)))
                return true
            }
            else -> {
                if (drawer_layout.isDrawerOpen(GravityCompat.START))
                    drawer_layout.closeDrawer(GravityCompat.START)
                else
                    drawer_layout.openDrawer(GravityCompat.START)
                }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        topLayout.visibility = View.GONE
        nav_view.menu.setGroupCheckable(0, true, true)
        val fragment = when (item.itemId) {
            R.id.nav_home -> AddMinsFragment.newInstance()
            R.id.nav_about_us -> AboutUsFragment.newInstance()
            else -> WriteUsFragment.newInstance()
        }
        when (item.itemId) {
            R.id.nav_home -> {
                topLayout.visibility = View.VISIBLE
                bottomNavView.menu.setGroupCheckable(0, true, true)
                AddMinsFragment.newInstance()
            }
            R.id.nav_about_us -> {
                bottomNavView.menu.setGroupCheckable(0, false, true)
                AboutUsFragment.newInstance()
            }
            else -> {
                bottomNavView.menu.setGroupCheckable(0, false, true)
                WriteUsFragment.newInstance()
            }
        }
        localFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private val onBottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        bottomNavView.menu.setGroupCheckable(0, true, true)
        nav_view.menu.getItem(0).isChecked = true
        topLayout.visibility = View.VISIBLE
        nav_view.hideKeyboard()
        val fragment = when (item.itemId) {
            R.id.navigation_add_mins -> AddMinsFragment.newInstance()
            R.id.navigation_gallery -> PicturesGalleryFragment.newInstance()
            R.id.navigation_complaints -> ChatFragment.newInstance()
            R.id.navigation_history -> HistoryFragment.newInstance()
            else -> AddMinsFragment.newInstance()
        }
        localFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit()
        true
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}