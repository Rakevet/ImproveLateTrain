package com.improve.latetrain.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.QuerySnapshot
import com.improve.latetrain.FirebaseConnection
import com.improve.latetrain.R
import com.improve.latetrain.activities.DrawerActivity
import com.improve.latetrain.adapters.GalleryAdapter
import com.improve.latetrain.data.AnalyticsInfo
import com.improve.latetrain.data.ImageFirestore
import kotlinx.android.synthetic.main.fragment_pictures_gallery.*
import kotlinx.android.synthetic.main.live_bar_layout.*

class PicturesGalleryFragment : Fragment() {

    private val TAG = "PicturesGalleryFragment"
    private val PICK_IMAGE = 1
    private val firebaseFunctions = FirebaseConnection()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pictures_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as DrawerActivity).getMinutes(minutes_nis_switch, live_minutes, textlivebar_tv)
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

        firebaseFunctions.imagesUrls.observe(this, Observer<QuerySnapshot>{result ->
            for(document in result){
                Log.d(TAG, "${document.id} => ${document.data}")
                imageReferences.add(ImageFirestore(document.id, document.data))
            }
            adapter.updateList(imageReferences)
        })
        firebaseFunctions.getImagesUrls()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            image_upload_loading_pb.visibility = View.VISIBLE
            upload_fab_fpg.hide()
            firebaseFunctions.uploadImageComplete.observe(this, Observer<String>{message ->
                image_upload_loading_pb.visibility = View.INVISIBLE
                upload_fab_fpg.show()
                if(message==firebaseFunctions.SUCCESS)
                    Toast.makeText(context, getString(R.string.image_uploaded_message), Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            })
            data.data?.let {
                firebaseFunctions.uploadImage(it)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): PicturesGalleryFragment =
            PicturesGalleryFragment()
    }
}