package com.improve.latetrain

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_pictures_gallery.*
import java.util.*

class PicturesGalleryFragment : Fragment() {

    private val TAG = "PicturesGalleryFragment"
    private var selectedPhoto: Uri? = null
    private val PICK_IMAGE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pictures_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        upload_fab_fpg.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, getString(R.string.upload_photo_gallery)), PICK_IMAGE)
        }

        var imageReferences: MutableList<StorageReference> = arrayListOf()
        val storage = FirebaseStorage.getInstance()
        val imagesRef = storage.reference.child("images")
        gallery_rv_fpg.layoutManager = LinearLayoutManager(activity?.applicationContext)
        val adapter = GalleryAdapter(imageReferences)
        gallery_rv_fpg.adapter = adapter
        imagesRef.listAll()
            .addOnSuccessListener { listResult ->
                imageReferences = listResult.items
                adapter.list = imageReferences
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d(TAG, "fail")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            selectedPhoto = data.data
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhoto ?: return)
                .addOnSuccessListener {
                    Log.d(TAG, "Upload image successfully: ${it.metadata?.path}")
                }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): PicturesGalleryFragment = PicturesGalleryFragment()
    }
}