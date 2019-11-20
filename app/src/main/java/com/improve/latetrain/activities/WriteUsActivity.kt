package com.improve.latetrain.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.improve.latetrain.R
import kotlinx.android.synthetic.main.activity_write_us.*

class WriteUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_us)

        setSupportActionBar(toolbar_awu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        send_email_btn_fwu.setOnClickListener {
            val email = getString(R.string.app_email)
            val subject = your_subject_et_fwu.text.toString()
            val text = text_body_et_fwu.text.toString()
            if (subject.isEmpty() || text.isEmpty())
                Snackbar.make(send_email_btn_fwu, getString(R.string.please_fill_all_fields_write_us), Snackbar.LENGTH_LONG).show()
            else sendEmail(send_email_btn_fwu, email, subject, text)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, DrawerActivity::class.java))
    }

    private fun sendEmail(view: View, email: String, subject: String, text: String){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email_write_us)))
        } catch (e: ActivityNotFoundException){
            Snackbar.make(view, getString(R.string.no_email_app_exist_write_us), Snackbar.LENGTH_LONG).show()
        }
    }
}