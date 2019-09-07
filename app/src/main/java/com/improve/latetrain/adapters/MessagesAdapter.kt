package com.improve.latetrain.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.improve.latetrain.Message
import com.improve.latetrain.R
import kotlinx.android.synthetic.main.message_layout_left.view.*

class MessagesAdapter(private val uid: String) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>(){

    var list: ArrayList<Message> =  ArrayList()

    enum class LayoutType(var type: Int){
        RIGHT_LAYOUT(0),
        LEFT_LAYOUT(1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (uid==list[position].uid)  LayoutType.RIGHT_LAYOUT.type else LayoutType.LEFT_LAYOUT.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (viewType== LayoutType.RIGHT_LAYOUT.type)
        {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.message_layout_right, parent, false)
            return MessageViewHolder(v)
        }
        val v = LayoutInflater.from(parent.context).inflate(R.layout.message_layout_left, parent, false)
        return MessageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return this.list.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.message.text = list[position].message
    }
    
    inner class MessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var message: TextView = itemView.message_tv
    }
}