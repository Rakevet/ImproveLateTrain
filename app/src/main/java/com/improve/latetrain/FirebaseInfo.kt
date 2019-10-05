package com.improve.latetrain

object FirebaseInfo {
    val TOTAL_TIME_PATH : String = if (!BuildConfig.DEBUG)  "totalMins" else "debug_total_mins"
    const val TOTAL_DAYS = "totalDays"
}