package com.charity.travelmantics.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.charity.travelmantics.R
import com.charity.travelmantics.ui.data.Destination

class UserAdapter :
    ListAdapter<Destination, UserAdapter.UserViewHolder>(DestinationDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val destination = getItem(position)
        holder.bindView(destination)
    }

    class DestinationDiffCallBack : DiffUtil.ItemCallback<Destination>() {

        override fun areItemsTheSame(oldItem: Destination, newItem: Destination): Boolean {
            return oldItem.city == newItem.city
        }

        override fun areContentsTheSame(oldItem: Destination, newItem: Destination): Boolean {
            return oldItem == newItem
        }

    }

    class UserViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val tvCity = view.findViewById<TextView>(R.id.tv_city)
        private val tvDesc = view.findViewById<TextView>(R.id.tv_desc)
        private val resortImage = view.findViewById<ImageView>(R.id.resort_image)

        fun bindView(destination: Destination) {
            tvCity.text = destination.city
            tvDesc.text = destination.description
            Glide.with(view.context)
                .load(destination.filePath)
                .apply(
                    RequestOptions().placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image))
                .into(resortImage)
        }

    }

}