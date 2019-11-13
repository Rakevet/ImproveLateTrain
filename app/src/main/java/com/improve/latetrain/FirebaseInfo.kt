package com.improve.latetrain

object FirebaseInfo {
    val TOTAL_TIME_PATH : String = if (!BuildConfig.DEBUG)  "totalMins" else "debug_total_mins"
    val TOTAL_DAYS = if (!BuildConfig.DEBUG) "totalDays" else "debug_total_days"
    const val IMAGES_WAITING_REF = "waiting_images"
    const val IMAGES_APPROVED_REF = "approved_images"
    val IMAGES_DOWNLOADING_REF = if(BuildConfig.BUILD_TYPE == "admin") IMAGES_WAITING_REF else IMAGES_APPROVED_REF
}