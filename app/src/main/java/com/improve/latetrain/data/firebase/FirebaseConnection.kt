package com.improve.latetrain.data.firebase

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.improve.latetrain.data.Message
import java.text.SimpleDateFormat
import java.util.*

class FirebaseConnection {

    private val realtimeInstance: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val totalMinutesPath: DatabaseReference by lazy {realtimeInstance.getReference(
        FirebaseInfo.TOTAL_TIME_PATH)}
    private val dailyPath: DatabaseReference by lazy {
        realtimeInstance.getReference(FirebaseInfo.TOTAL_DAYS)
            .child(currentYear).child(currentMonth).child(currentDay)
    }
    val totalWaitingMinutes: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val chatMessages: MutableLiveData<Message> by lazy { MutableLiveData<Message>() }
    val uploadMinutesComplete: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val minutesPerDay: MutableLiveData<Pair<Int, String>> by lazy {
        MutableLiveData<Pair<Int, String>>()
    }
    val imagesUrls: MutableLiveData<QuerySnapshot> by lazy {MutableLiveData<QuerySnapshot>()}
    val uploadImageComplete: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private var dailyMinutesListener: ChildEventListener? = null
    private var totalWaitingMinutesListener: ValueEventListener? = null
    private var messagesListener: ChildEventListener? = null
    private val currentDay = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
    private val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
    private val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
    val SUCCESS = "SUCCESS"

    fun adminDeleteImageFromStorage(imageRef: String){
        storageInstance.reference.child("images/$imageRef").delete()
    }

    fun adminDeleteWaitingImage(id: String){
        firestoreInstance.collection(FirebaseInfo.IMAGES_WAITING_REF).document(id).delete()
    }

    fun adminUploadApprovedImage(map: HashMap<String, String>){
        firestoreInstance.collection(FirebaseInfo.IMAGES_APPROVED_REF).add(map)
    }

    fun uploadImage(image: Uri){
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        var photoRef: String? = ""
        ref.putFile(image).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            photoRef = task.result?.storage?.name
            ref.downloadUrl
        }.addOnSuccessListener {
            firestoreInstance.collection(FirebaseInfo.IMAGES_WAITING_REF).add(hashMapOf("link" to it.toString(), "ref" to photoRef))
                .addOnCompleteListener {completeTask ->
                    if(completeTask.isSuccessful)
                        uploadImageComplete.value = SUCCESS
                    else
                        uploadImageComplete.value = completeTask.exception.toString()
                }
        }
    }

    fun getImagesUrls(){
        firestoreInstance.collection(FirebaseInfo.IMAGES_DOWNLOADING_REF).get()
            .addOnSuccessListener {result->
                imagesUrls.value = result
            }
    }

    fun getDailyMinutes(){
        dailyMinutesListener = object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val minutes = dataSnapshot.child("minutes").getValue(Int::class.java)
                val day = dataSnapshot.ref.key
                if(minutes!=null && day!=null)
                    minutesPerDay.value = Pair(minutes, day)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        dailyMinutesListener?.let {
            dailyPath.parent?.addChildEventListener(it)
        }
    }

    fun removeDailyMinutesListener(){
        dailyMinutesListener?.let {
            dailyPath.parent?.removeEventListener(it)
        }
    }

    fun getTotalWaitingMinutes() {
        totalWaitingMinutesListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                totalWaitingMinutes.value = "${p0.value}"
            }
        }
        totalWaitingMinutesListener?.let {
            totalMinutesPath.addValueEventListener(it)
        }
    }

    fun removeTotalWaitingMinutesListener(){
        totalWaitingMinutesListener?.let {
            totalMinutesPath.removeEventListener(it)
        }
    }

    private fun uploadTotalDays(minutes: Int) {
        dailyPath.child("minutes")
            .runTransaction(object : Transaction.Handler {
                override fun onComplete(error: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                    if (error == null)
                        uploadMinutesComplete.value = SUCCESS
                    else
                        uploadMinutesComplete.value = error.message
                }
                override fun doTransaction(p0: MutableData): Transaction.Result {
                    if (p0.getValue(Int::class.java) == null) {
                        p0.value = minutes
                        return Transaction.success(p0)
                    }
                    val dayData = p0.getValue(Int::class.java)
                    p0.value = dayData?.plus(minutes)
                    return Transaction.success(p0)
                }
            })
    }

    fun uploadMinutes(minutes: Int) {
        val totalWaitingPath = realtimeInstance.getReference(FirebaseInfo.TOTAL_TIME_PATH)
        totalWaitingPath.runTransaction(object : Transaction.Handler {
            override fun onComplete(error: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                if (error == null)
                    uploadTotalDays(minutes)
                else
                    uploadMinutesComplete.value = error.message
            }
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val data = mutableData.getValue(Int::class.java)
                if (data == null) {
                    mutableData.value = minutes
                    return Transaction.success(mutableData)
                }
                mutableData.value = data.plus(minutes)
                return Transaction.success(mutableData)
            }
        })
    }

    fun uploadMessage(message: Message){
        dailyPath.child("messages").push().setValue(message)
    }

    fun listenToMessages(){
        messagesListener = object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val message = dataSnapshot.getValue(Message::class.java)
                message?.let{
                    chatMessages.value = it
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        messagesListener?.let {
            dailyPath.child("messages").addChildEventListener(it)
        }
    }

    fun removeMessagesListener(){
        messagesListener?.let {
            dailyPath.child("messages").removeEventListener(it)
        }
    }
}