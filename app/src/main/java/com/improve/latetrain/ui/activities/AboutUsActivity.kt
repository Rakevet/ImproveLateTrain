package com.improve.latetrain.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.improve.latetrain.R
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlinx.android.synthetic.main.activity_about_us.*

class AboutUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        setSupportActionBar(toolbar_aau)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        licenses_btn.setOnClickListener {
            LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .start(this)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, DrawerActivity::class.java))
    }
}