package com.improve.latetrain

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.message_layout.view.*

class MessagesAdapter(val uid: String) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>(){

    var list: ArrayList<Message> =  ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.message_layout, parent, false)
        return MessageViewHolder(v)
    }

    override fun getItemCount(): Int {
        return this.list.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.message.text = list[position].message
        if(list[position].uid==uid)
        {
            val constraintSet = ConstraintSet()
            constraintSet.clone(holder.constraintPart)
            constraintSet.connect(R.id.message_tv, ConstraintSet.LEFT, R.id.message_constraint_part, ConstraintSet.LEFT)
            constraintSet.clear(R.id.message_tv, ConstraintSet.RIGHT)
            constraintSet.applyTo(holder.constraintPart)

            constraintSet.clone(holder.constraintHolder)
            constraintSet.connect(R.id.message_constraint_part, ConstraintSet.LEFT, R.id.message_constraint_holder, ConstraintSet.LEFT)
            constraintSet.connect(R.id.message_constraint_part, ConstraintSet.RIGHT, R.id.right_guideline_ml, ConstraintSet.LEFT)
            constraintSet.applyTo(holder.constraintHolder)
        }
        else {
            val constraintSet = ConstraintSet()
            constraintSet.clone(holder.constraintPart)
            constraintSet.connect(R.id.message_tv, ConstraintSet.RIGHT, R.id.message_constraint_part, ConstraintSet.RIGHT)
            constraintSet.clear(R.id.message_tv, ConstraintSet.LEFT)
            constraintSet.applyTo(holder.constraintPart)

            constraintSet.clone(holder.constraintHolder)
            constraintSet.connect(R.id.message_constraint_part, ConstraintSet.RIGHT, R.id.message_constraint_holder, ConstraintSet.RIGHT)
            constraintSet.connect(R.id.message_constraint_part, ConstraintSet.LEFT, R.id.left_guideline_ml, ConstraintSet.RIGHT)
            constraintSet.applyTo(holder.constraintHolder)
        }
    }
    
    inner class MessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var message = itemView.message_tv
        var constraintHolder = itemView.message_constraint_holder
        var constraintPart = itemView.message_constraint_part
    }
}