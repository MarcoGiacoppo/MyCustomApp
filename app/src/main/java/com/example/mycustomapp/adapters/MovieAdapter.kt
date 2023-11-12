package com.example.mycustomapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycustomapp.databinding.MovieItemBinding
import com.example.mycustomapp.models.Movie

class MovieAdapter(
    private val movies : List<Movie>,
    private val itemClickListener: OnItemClickListener
    ) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>(){

    // ViewHolder class for individual movie items
    class MovieViewHolder(private val binding: MovieItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val IMAGE_BASE = "https://image.tmdb.org/t/p/w500/"

        // Bind movie data to the ViewHolder
        fun bindMovie(movie: Movie) {
            // Load movie poster image using Glide library
            Glide.with(binding.root).load(IMAGE_BASE + movie.poster).into(binding.moviePoster)
        }
    }

    // Interface for item click events
    interface OnItemClickListener {
        fun onItemClick(movie: Movie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        // Inflate the layout for an individual movie item
        val binding = MovieItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun getItemCount(): Int = movies.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        // Bind data to the ViewHolder
        holder.bindMovie(movies[position])

        // Set a click listener on the item view
        holder.itemView.setOnClickListener{
            itemClickListener.onItemClick(movies[position])
        }
    }
}
