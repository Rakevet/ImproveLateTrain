package com.improve.latetrain

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SpinnerAdapter(context: Context, layoutResource: Int, var items: List<String>) :
    ArrayAdapter<String>(context, layoutResource, items) {

   private val font: Typeface = Typeface.createFromAsset(context.assets, "font/comixno2_medium.ttf")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =  super.getView(position, convertView, parent) as TextView
        view.typeface = font
        if (items[0]=="0")
            view.gravity = Gravity.CENTER
        return view
    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =  super.getView(position, convertView, parent) as TextView
        view.typeface = font
        if (items[0]=="0")
            view.gravity = Gravity.CENTER
        return view
    }
}