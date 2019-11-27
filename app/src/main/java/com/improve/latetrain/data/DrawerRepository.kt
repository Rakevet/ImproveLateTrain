package com.improve.latetrain.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.QuerySnapshot
import com.improve.latetrain.data.Event
import com.improve.latetrain.data.entities.LiveTotalMinutes
import com.improve.latetrain.data.db.LiveTotalMinutesDao
import com.improve.latetrain.data.entities.Message
import com.improve.latetrain.data.firebase.FirebaseConnection
import com.improve.latetrain.data.firebase.IFirebaseObserver
import kotlinx.coroutines.CoroutineScope
import java.util.HashMap

class DrawerRepository(private val liveTotalMinutesDao: LiveTotalMinutesDao) : IFirebaseObserver {

    private val firebaseConnection = FirebaseConnection(this)

    private val _minutesPerDay: MutableLiveData<Event<Pair<Int, String>>> by lazy {
        MutableLiveData<Event<Pair<Int, String>>>()
    }
    private val _uploadMinutesComplete: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private val _chatMessages: MutableLiveData<Event<Message>> by lazy { MutableLiveData<Event<Message>>() }
    private val _imagesUrls: MutableLiveData<Event<QuerySnapshot>> by lazy { MutableLiveData<Event<QuerySnapshot>>() }
    private val _uploadImageComplete: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val uploadImageComplete: LiveData<String> get() = _uploadImageComplete
    val imagesUrls: MutableLiveData<Event<QuerySnapshot>> get() = _imagesUrls
    val chatMessages: LiveData<Event<Message>> get() = _chatMessages
    val uploadMinutesComplete: LiveData<String> get() = _uploadMinutesComplete
    val minutesPerDay: LiveData<Event<Pair<Int, String>>> get() = _minutesPerDay
    val totalMinutes: LiveData<LiveTotalMinutes> = liveTotalMinutesDao.getLiveTotalMinutes()

    fun adminUploadApprovedImage(map: HashMap<String, String>){
        firebaseConnection.adminUploadApprovedImage(map)
    }

    fun adminDeleteWaitingImage(id: String){
        firebaseConnection.adminDeleteWaitingImage(id)
    }

    fun adminDeleteImageFromStorage(imageRef: String){
        firebaseConnection.adminDeleteImageFromStorage(imageRef)
    }

    override fun uploadImageComplete(message: String) {
        _uploadImageComplete.value = message
    }

    fun uploadImage(image: Uri){
        firebaseConnection.uploadImage(image)
    }

    fun getImagesUrls(){
        firebaseConnection.getImagesUrls()
    }

    override fun getImagesSuccess(result: QuerySnapshot) {
        _imagesUrls.value = Event(result)
    }

    fun startListeningForMessages(){
        firebaseConnection.listenToMessages()
    }

    fun stopListeningForMessages(){
        firebaseConnection.removeMessagesListener()
    }

    override fun messageChildAdded(message: Message) {
        _chatMessages.value = Event(message)
    }

    fun uploadMessage(message: Message){
        firebaseConnection.uploadMessage(message)
    }

    override fun uploadMinutesComplete(message: String) {
        _uploadMinutesComplete.value = message
    }

    fun uploadMinutes(minutes: Int){
        firebaseConnection.uploadMinutes(minutes)
    }

    override fun dailyMinutesChildAdded(pair: Pair<Int, String>) {
        _minutesPerDay.value = Event(pair)
    }

    fun startListeningForDailyMinutes(){
        firebaseConnection.getDailyMinutes()
    }

    fun stopListeningForDailyMinutes(){
        firebaseConnection.removeDailyMinutesListener()
    }

    override suspend fun totalMinutesChanged(minutes: Long) {
        insertTotalMinutes(minutes)
    }

    fun startListeningForTotalWaitingMinutes(scope: CoroutineScope) {
        firebaseConnection.getTotalWaitingMinutes(scope)
    }

    fun stopListeningForTotalWaitingMinutes() {
        firebaseConnection.removeTotalWaitingMinutesListener()
    }

    private suspend fun insertTotalMinutes(minutes: Long) {
        val liveTotalMinutes =
            LiveTotalMinutes(1, minutes)
        liveTotalMinutesDao.insertLiveTotalMinutes(liveTotalMinutes)
    }
}