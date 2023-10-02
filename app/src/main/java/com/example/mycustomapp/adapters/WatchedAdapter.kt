package com.example.mycustomapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.mycustomapp.databinding.WatchedItemBinding
import com.example.mycustomapp.models.WatchlistItem
import com.bumptech.glide.Glide

class WatchedAdapter(
    private val watchlist: List<WatchlistItem>,
    private val itemClickListener: OnItemClickListener,
    private val deleteItemClickListener: OnDeleteItemClickListener
) : RecyclerView.Adapter<WatchedAdapter.WatchlistViewHolder>() {

    // ViewHolder class for individual watched items
    class WatchlistViewHolder(
        private val binding: WatchedItemBinding,
        private val deleteItemClickListener: OnDeleteItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private val IMAGE_BASE = "https://image.tmdb.org/t/p/w500/"

        // Bind watched item data to the ViewHolder
        fun bindWatchlistItem(watchlistItem: WatchlistItem) {
            binding.movieTitleTextView.text = watchlistItem.movieTitle
            binding.movieRatingBar.rating = watchlistItem.userRating
            binding.movieReviewTextView.text = watchlistItem.userReview
            // Load movie poster image using Glide library
            Glide.with(binding.root.context).load(IMAGE_BASE + watchlistItem.posterUrl).into(binding.moviePosterImageView)

            val deleteButton: Button = binding.deleteButton
            // Set a click listener on the delete button to handle item deletion
            deleteButton.setOnClickListener {
                deleteItemClickListener.onDeleteItemClick(watchlistItem)
            }
        }
    }

    // Interface for item click events
    interface OnItemClickListener {
        fun onItemClick(watchlistItem: WatchlistItem)
    }

    // Interface for delete item click events
    interface OnDeleteItemClickListener {
        fun onDeleteItemClick(watchlistItem: WatchlistItem)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        // Inflate the layout for an individual watched item
        val binding = WatchedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WatchlistViewHolder(binding, deleteItemClickListener)
    }

    override fun getItemCount(): Int = watchlist.size

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {


        // Bind data to the ViewHolder
        holder.bindWatchlistItem(watchlist[position])

        // Set a click listener on the item view to handle item click events
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(watchlist[position])
        }
    }
}
