package com.improve.latetrain.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.improve.latetrain.*
import com.improve.latetrain.adapters.GalleryAdapter
import kotlinx.android.synthetic.main.fragment_pictures_gallery.*
import kotlinx.android.synthetic.main.live_bar_layout.*
import java.util.*

class PicturesGalleryFragment : Fragment() {

    private val TAG = "PicturesGalleryFragment"
    private var selectedPhoto: Uri? = null
    private val PICK_IMAGE = 1
    private val firestoreDB = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pictures_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as DrawerActivity).getMinutes(live_minutes)
        (activity as DrawerActivity).setSwitch(minutes_nis_switch, live_minutes, textlivebar_tv)

        upload_fab_fpg.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(
                    intent, getString(R.string.upload_photo_gallery)), PICK_IMAGE)
            context?.let{
                AnalyticsInfo.sendAnalytics("uploadImageBtn", arrayListOf(Pair("", "")), it)
            }
        }

        val imageReferences: MutableList<ImageFirestore> = arrayListOf()
        gallery_rv_fpg.layoutManager = LinearLayoutManager(activity?.applicationContext)
        val adapter = GalleryAdapter(imageReferences)
        gallery_rv_fpg.adapter = adapter
        firestoreDB.collection(FirebaseInfo.IMAGES_DOWNLOADING_REF).get()
            .addOnSuccessListener {result->
                for(document in result){
                    Log.d(TAG, "${document.id} => ${document.data}")
                    imageReferences.add(ImageFirestore(document.id, document.data))
                }
                adapter.updateList(imageReferences)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            image_upload_loading_pb.visibility = View.VISIBLE
            upload_fab_fpg.hide()

            selectedPhoto = data.data
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            var photoRef: String? = ""
            ref.putFile(selectedPhoto ?: return).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                photoRef = task.result?.storage?.name
                ref.downloadUrl
            }.addOnSuccessListener {
                firestoreDB.collection(FirebaseInfo.IMAGES_WAITING_REF).add(hashMapOf("link" to it.toString(), "ref" to photoRef))
                    .addOnSuccessListener {
                        image_upload_loading_pb.visibility = View.INVISIBLE
                        upload_fab_fpg.show()
                        Toast.makeText(context, getString(R.string.image_uploaded_message), Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): PicturesGalleryFragment =
            PicturesGalleryFragment()
    }
}