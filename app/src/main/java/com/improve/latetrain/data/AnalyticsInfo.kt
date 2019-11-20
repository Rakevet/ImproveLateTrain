package com.improve.latetrain.data

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsInfo{
    private var INSTANCE: FirebaseAnalytics? = null

    private fun getInstance(context: Context): FirebaseAnalytics{
        if(INSTANCE ==null)
            INSTANCE = FirebaseAnalytics.getInstance(context)
        return INSTANCE as FirebaseAnalytics
    }

    fun sendAnalytics(key: String, info: ArrayList<Pair<String, String>>, context: Context){
        val bundle = Bundle()
        for(i in 0 until info.size){
            bundle.putString(info[i].first, info[i].second)
        }
        getInstance(context).logEvent(key, bundle)
    }
}