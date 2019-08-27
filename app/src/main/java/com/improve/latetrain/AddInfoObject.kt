package com.improve.latetrain

class AddInfoObject() {
    var minutes: Int = 0
    var uid: String = ""
    var station: String = ""
    var destination: String = ""
    var timestamp: Long = 0
    constructor(minutes: Int, uid: String, station: String, destination: String, timestamp: Long): this(){
        this.minutes = minutes
        this.uid = uid
        this.station = station
        this.destination = destination
        this.timestamp = timestamp
    }
}