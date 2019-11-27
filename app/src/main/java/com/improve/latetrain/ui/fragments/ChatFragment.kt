package com.improve.latetrain.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.improve.latetrain.viewmodel.DrawerViewModel
import com.improve.latetrain.R
import com.improve.latetrain.data.Event
import com.improve.latetrain.ui.activities.DrawerActivity
import com.improve.latetrain.ui.adapters.MessagesAdapter
import com.improve.latetrain.data.firebase.AnalyticsInfo
import com.improve.latetrain.data.entities.Message
import kotlinx.android.synthetic.main.content_drawer.*
import kotlinx.android.synthetic.main.fragment_chat.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatFragment : Fragment() {

    private val drawerViewModel: DrawerViewModel by viewModel()
    private val uid = Settings.Secure.getString(activity?.contentResolver, Settings.Secure.ANDROID_ID)
    private val adapter = MessagesAdapter(uid)
    private lateinit var messagesObserver: Observer<Event<Message>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val linearLayoutManager = LinearLayoutManager(activity?.applicationContext, RecyclerView.VERTICAL, false)
        rv.layoutManager = linearLayoutManager
        rv.adapter = adapter

        send_btn.setOnClickListener {
            if(write_et.text.toString().isNotBlank()){
                val message = Message(
                    write_et.text.toString().trim(),
                    uid
                )
                drawerViewModel.uploadMessage(message)
                write_et.text.clear()
                context?.let{
                    AnalyticsInfo.sendAnalytics("messageSendBtn", arrayListOf(Pair("", "")), it)
                }
            }
        }
        write_et.setOnClickListener {
            (context as DrawerActivity).live_bar?.animate()
                ?.translationY(30f)
                ?.setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        if((context as DrawerActivity).live_bar?.visibility==View.VISIBLE)
                            (context as DrawerActivity).live_bar?.visibility = View.GONE
                    }
                })
            (context as DrawerActivity).bottom_nav_view?.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        messagesObserver = Observer{event ->
            event.getContentIfNotHandled()?.let {message->
                adapter.addMessage(message)
                rv?.smoothScrollToPosition(adapter.list.size - 1)
            }
        }
        drawerViewModel.chatMessages.observe(this, messagesObserver)
        drawerViewModel.startListeningForMessages()
    }

    override fun onStop() {
        super.onStop()
        drawerViewModel.stopListeningForMessages()
        drawerViewModel.chatMessages.removeObserver(messagesObserver)
    }

    companion object {
        @JvmStatic
        fun newInstance(): ChatFragment = ChatFragment()
    }
}