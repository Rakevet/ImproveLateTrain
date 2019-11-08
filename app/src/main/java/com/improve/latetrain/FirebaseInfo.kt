package com.improve.latetrain

object FirebaseInfo {
    val TOTAL_TIME_PATH : String = if (!BuildConfig.DEBUG)  "totalMins" else "debug_total_mins"
    val TOTAL_DAYS = if (!BuildConfig.DEBUG) "totalDays" else "debug_total_days"
    val IMAGES_DOWNLOADING_REF = if(BuildConfig.BUILD_TYPE == "admin") "waiting_images" else "approved_images"
}