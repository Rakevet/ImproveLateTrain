package com.improve.latetrain


import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_complaints.*


class ComplaintsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_complaints, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Get reference to firebase database at "Messages"
        val instance = FirebaseDatabase.getInstance()
        val ref = instance.getReference("Messages/")

        val uid = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)

        var linearLayoutManager = LinearLayoutManager(activity!!.applicationContext, RecyclerView.VERTICAL, false)
        rv.layoutManager = linearLayoutManager
        var adapter = MessagesAdapter(uid)
        rv.adapter = adapter
        rv.scrollToPosition(adapter.list.size - 1)

        val postListener = object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                var message = dataSnapshot.getValue(Message::class.java)
                adapter.list.add(message!!)
                rv.scrollToPosition(adapter.list.size - 1)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        ref.addChildEventListener(postListener)
        send_btn.setOnClickListener {
            if(write_et.text.toString()!=""){
                var message = Message(write_et.text.toString(), uid)
                ref.push().setValue(message)
                write_et.setText("")
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = ComplaintsFragment()

    }
}
