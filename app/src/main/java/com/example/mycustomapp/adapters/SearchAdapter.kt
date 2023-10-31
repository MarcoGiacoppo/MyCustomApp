package com.example.mycustomapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycustomapp.databinding.SearchResultItemBinding
import com.example.mycustomapp.models.Movie

class SearchResultsAdapter(
    private val results: List<Movie>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SearchResultsAdapter.ResultViewHolder>() {

    // ViewHolder class for individual movie items
    inner class ResultViewHolder(private val binding: SearchResultItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val IMAGE_BASE = "https://image.tmdb.org/t/p/w500/"

        // Bind movie data to the ViewHolder
        fun bindResult(result: Movie) {
            // Load movie poster image using Glide library
            Glide.with(binding.root).load(IMAGE_BASE + result.poster).into(binding.searchPoster)
        }
    }

    // Interface for item click events
    interface OnItemClickListener {
        fun onItemClick(result: Movie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        // Inflate the layout for an individual movie item using the binding
        val binding = SearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(binding)
    }

    override fun getItemCount(): Int = results.size

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        // Bind data to the ViewHolder
        holder.bindResult(results[position])

        // Set a click listener on the item view
        holder.itemView.setOnClickListener{
            itemClickListener.onItemClick(results[position])
        }
    }
}
