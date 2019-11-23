package com.improve.latetrain.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.improve.latetrain.BuildConfig
import com.improve.latetrain.data.firebase.FirebaseConnection
import com.improve.latetrain.GlideApp
import com.improve.latetrain.R
import com.improve.latetrain.data.ImageFirestore
import kotlinx.android.synthetic.main.gallery_row_layout.view.*

class GalleryAdapter(var list: MutableList<ImageFirestore>): RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private val firebaseFunctions = FirebaseConnection()

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

    fun delete(position: Int){
        firebaseFunctions.adminDeleteWaitingImage(list[position].id)
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
                    builder.setPositiveButton(context.getString(R.string.approve_pic)){ _, _ ->
                        firebaseFunctions.adminUploadApprovedImage(
                            hashMapOf("link" to list[position].content["link"].toString(),
                            "ref" to list[position].content["ref"].toString()))
                        delete(position)
                    }
                    builder.setNegativeButton(context.getString(R.string.delete_pic)){ _, _ ->
                        firebaseFunctions.adminDeleteImageFromStorage(list[position].content["ref"].toString())
                        delete(position)
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }
    }
}