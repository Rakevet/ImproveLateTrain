package com.improve.latetrain.fragments

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.improve.latetrain.AnalyticsInfo
import com.improve.latetrain.FirebaseInfo
import com.improve.latetrain.Message
import com.improve.latetrain.R
import com.improve.latetrain.adapters.MessagesAdapter
import kotlinx.android.synthetic.main.fragment_chat.*
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val instance = FirebaseDatabase.getInstance()
        val currentDay = SimpleDateFormat("dd", Locale.getDefault()).format(Date()).toInt()
        val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date()).toInt()
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()).toInt()
        val messagesPerDaysPath = instance.getReference(FirebaseInfo.TOTAL_DAYS)
            .child(currentYear.toString()).child(currentMonth.toString())
            .child(currentDay.toString()).child("messages")

        val uid = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)

        val linearLayoutManager = LinearLayoutManager(activity?.applicationContext, RecyclerView.VERTICAL, false)
        rv.layoutManager = linearLayoutManager
        val adapter = MessagesAdapter(uid)
        rv.adapter = adapter
        rv.scrollToPosition(adapter.list.size - 1)

        val postListener = object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val message = dataSnapshot.getValue(Message::class.java)
                message?.let{
                    adapter.addMessage(message)
                }


                rv?.smoothScrollToPosition(adapter.list.size - 1)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        messagesPerDaysPath.addChildEventListener(postListener)
        send_btn.setOnClickListener {
            if(write_et.text.toString().isNotBlank()){
                val message = Message()
                message.message = write_et.text.toString().trim()
                message.uid = uid
                messagesPerDaysPath.push().setValue(message)
                write_et.text.clear()
                send_btn.hideKeyboard()
                context?.let{
                    AnalyticsInfo.sendAnalytics("messageSendBtn", arrayListOf(Pair("", "")), it)
                }
            }
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    companion object {
        @JvmStatic
        fun newInstance(): ChatFragment = ChatFragment()
    }
}