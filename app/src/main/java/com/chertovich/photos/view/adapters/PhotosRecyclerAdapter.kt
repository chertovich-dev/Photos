package com.chertovich.photos.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chertovich.photos.R
import com.chertovich.photos.WRONG_INDEX
import com.chertovich.photos.data.Photo
import com.chertovich.photos.data.PhotoState
import com.chertovich.photos.serverDateToDate
import com.chertovich.photos.setPhotoToImageView
import java.text.SimpleDateFormat
import java.util.Date

private const val format = "dd.MM.yyyy"

interface OnPhotosRecyclerListener {
    fun onLoadPhoto(index: Int)
    fun onSelectPhoto(index: Int)
}

class PhotosRecyclerAdapter(private val photos: List<Photo>, private val listener: OnPhotosRecyclerListener) :
    RecyclerView.Adapter<PhotosRecyclerAdapter.PhotosViewHolder>() {

    private val dateFormat = SimpleDateFormat(format)

    private fun getIndex(viewHolder: PhotosViewHolder): Int {
        val index = viewHolder.absoluteAdapterPosition

        return if (index in photos.indices) {
            index
        } else {
            WRONG_INDEX
        }
    }

    private fun loadImage(holder: PhotosViewHolder, index: Int) {
        try {
            val photo = photos[index]

            if (photo.state == PhotoState.REFRESH) {
                photo.state = PhotoState.LOADED
            }

            setPhotoToImageView(holder.imageView, photo)
        } catch (e: Exception) {
            //
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_photos, parent, false)
        val photosViewHolder = PhotosViewHolder(itemView)

        photosViewHolder.imageView.setOnClickListener {
            val index = getIndex(photosViewHolder)

            if (index != WRONG_INDEX) {
                listener.onSelectPhoto(index)
            }
        }

        return photosViewHolder
    }

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        if (position in photos.indices) {
            val photo = photos[position]
            val date = Date(serverDateToDate(photo.image.date))
            holder.textView.text = dateFormat.format(date)

            when (photo.state) {
                PhotoState.EMPTY -> listener.onLoadPhoto(position)
                PhotoState.REFRESH, PhotoState.LOADED -> loadImage(holder, position)
                else -> {}
            }
        }
    }

    class PhotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        val textView = itemView.findViewById<TextView>(R.id.textView)
    }
}