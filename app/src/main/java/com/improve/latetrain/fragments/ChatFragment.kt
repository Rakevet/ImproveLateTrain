package com.improve.latetrain.fragments

import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.improve.latetrain.FirebaseInfo
import com.improve.latetrain.Message
import com.improve.latetrain.adapters.MessagesAdapter
import com.improve.latetrain.R
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
                    adapter.list.add(message)
                }
                rv?.scrollToPosition(adapter.list.size - 1)
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
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): ChatFragment = ChatFragment()
    }
}