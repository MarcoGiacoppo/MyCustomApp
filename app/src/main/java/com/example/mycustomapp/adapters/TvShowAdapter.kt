package com.example.mycustomapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycustomapp.databinding.MovieItemBinding
import com.example.mycustomapp.models.TVShow

class TVShowAdapter(
    private val tvShows: List<TVShow>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<TVShowAdapter.TVShowViewHolder>() {

    // ViewHolder class for individual TV show items
    class TVShowViewHolder(private val binding: MovieItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val IMAGE_BASE = "https://image.tmdb.org/t/p/w500/"

        // Bind TV show data to the ViewHolder
        fun bindTVShow(tvShow: TVShow) {
            // Load TV show poster image using Glide library
            Glide.with(binding.root).load(IMAGE_BASE + tvShow.poster).into(binding.moviePoster)
        }
    }

    // Interface for item click events
    interface OnItemClickListener {
        fun onItemClick(tvShow: TVShow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TVShowViewHolder {
        // Inflate the layout for an individual TV show item
        val binding = MovieItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TVShowViewHolder(binding)
    }

    override fun getItemCount(): Int = tvShows.size

    override fun onBindViewHolder(holder: TVShowViewHolder, position: Int) {
        // Bind data to the ViewHolder
        holder.bindTVShow(tvShows[position])

        // Set a click listener on the item view
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(tvShows[position])
        }
    }
}
