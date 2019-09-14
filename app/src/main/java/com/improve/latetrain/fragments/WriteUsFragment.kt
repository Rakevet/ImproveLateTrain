package com.improve.latetrain.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.improve.latetrain.R
import kotlinx.android.synthetic.main.fragment_write_us.*

class WriteUsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_write_us, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        send_email_btn_fwu.setOnClickListener {
            val email = getString(R.string.app_email)
            val subject = your_subject_et_fwu.text.toString()
            val text = text_body_et_fwu.text.toString()
            if (subject.isEmpty() || text.isEmpty())
                Snackbar.make(send_email_btn_fwu, getString(R.string.please_fill_all_fields_write_us), Snackbar.LENGTH_LONG).show()
            else sendEmail(send_email_btn_fwu, email, subject, text)
        }
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

    companion object{
        @JvmStatic
        fun newInstance(): WriteUsFragment =
            WriteUsFragment()
    }
}