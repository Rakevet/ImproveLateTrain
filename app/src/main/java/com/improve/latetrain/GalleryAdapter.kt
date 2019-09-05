package com.improve.latetrain

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.gallery_row_layout.view.*

class GalleryAdapter(var list: MutableList<StorageReference>): RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_row_layout, parent, false)
        return GalleryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        GlideApp.with(holder.context)
            .load(list[position])
            .apply(RequestOptions.overrideOf(holder.itemView.width,holder.itemView.height))
            .centerCrop()
            .into(holder.image)
    }

    inner class GalleryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.image_iv_grl
        val context: Context = itemView.context
    }
}