package com.improve.latetrain.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.QuerySnapshot
import com.improve.latetrain.data.DrawerRepository
import com.improve.latetrain.data.Event
import com.improve.latetrain.data.entities.LiveTotalMinutes
import com.improve.latetrain.data.entities.Message
import java.util.HashMap

class DrawerViewModel(private val repository: DrawerRepository, application: Application):
    AndroidViewModel(application) {

    private val scope = viewModelScope

    val uploadMinutesComplete: LiveData<String> by lazy { repository.uploadMinutesComplete }
    val totalMinutes: LiveData<LiveTotalMinutes> by lazy { repository.totalMinutes }
    val minutesPerDay: LiveData<Event<Pair<Int, String>>> by lazy { repository.minutesPerDay }
    val chatMessages: LiveData<Event<Message>> by lazy { repository.chatMessages }
    val imagesUrls: LiveData<Event<QuerySnapshot>> by lazy { repository.imagesUrls }
    val uploadImageComplete: LiveData<String> by lazy { repository.uploadImageComplete }

    fun adminUploadApprovedImage(map: HashMap<String, String>){
        repository.adminUploadApprovedImage(map)
    }

    fun adminDeleteWaitingImage(id: String){
        repository.adminDeleteWaitingImage(id)
    }

    fun adminDeleteImageFromStorage(imageRef: String){
        repository.adminDeleteImageFromStorage(imageRef)
    }

    fun uploadImage(image: Uri){
        repository.uploadImage(image)
    }

    fun getImagesUrls(){
        repository.getImagesUrls()
    }

    fun startListeningForMessages(){
        repository.startListeningForMessages()
    }

    fun stopListeningForMessages(){
        repository.stopListeningForMessages()
    }

    fun uploadMessage(message: Message){
        repository.uploadMessage(message)
    }

    fun uploadMinutes(minutes: Int){
        repository.uploadMinutes(minutes)
    }

    fun startListeningForDailyMinutes(){
        repository.startListeningForDailyMinutes()
    }

    fun stopListeningForDailyMinutes(){
        repository.stopListeningForDailyMinutes()
    }

    fun startListeningForTotalWaitingMinutes(){
        repository.startListeningForTotalWaitingMinutes(scope)
    }

    fun stopListeningForTotalWaitingMinutes(){
        repository.stopListeningForTotalWaitingMinutes()
    }
}