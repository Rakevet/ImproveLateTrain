package com.improve.latetrain.adapters

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.improve.latetrain.*
import kotlinx.android.synthetic.main.gallery_row_layout.view.*

class GalleryAdapter(var list: MutableList<ImageFirestore>): RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private val TAG = "GalleryAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_row_layout, parent, false)
        return GalleryViewHolder(view)
    }

    fun updateList(_list: MutableList<ImageFirestore>){
        list = _list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val currentImage = list[position]
        GlideApp.with(holder.context)
            .load(currentImage.content["link"].toString())
            .apply(RequestOptions.overrideOf(holder.itemView.width,holder.itemView.height))
            .centerCrop()
            .into(holder.image)
    }

    fun deleteFromStorage(position: Int){
        val storageRef = FirebaseStorage.getInstance().reference
        storageRef.child("images/${list[position].content["ref"]}")
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
    }

    fun delete(position: Int){
        val database = FirebaseFirestore.getInstance()
        database.collection(FirebaseInfo.IMAGES_WAITING_REF).document(list[position].id)
            .delete()
            .addOnSuccessListener {}
        list.removeAt(position)
        notifyDataSetChanged()
    }

    inner class GalleryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.image_iv_grl
        val context: Context = itemView.context
        init {

            if(BuildConfig.BUILD_TYPE=="admin"){
                itemView.setOnClickListener {
                    val position = adapterPosition
                    val builder = AlertDialog.Builder(itemView.context)
                    builder.setTitle(context.getString(R.string.approve_delete_pic))
                    builder.setMessage(context.getString(R.string.approve_delete_pic_message))
                    builder.setPositiveButton(context.getString(R.string.approve_pic)){ dialog, _ ->
                        val database = FirebaseFirestore.getInstance()
                        database.collection(FirebaseInfo.IMAGES_APPROVED_REF)
                            .add(hashMapOf("link" to list[position].content["link"].toString(),
                                            "ref" to list[position].content["ref"].toString()))
                        delete(position)
                    }
                    builder.setNegativeButton(context.getString(R.string.delete_pic)){ dialog, _ ->
                        deleteFromStorage(position)
                        delete(position)
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }
    }
}