package com.example.mycustomapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.mycustomapp.models.Movie
import com.example.mycustomapp.models.WatchlistItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class DetailActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var movieReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Retrieve the movie details from the Intent extras
        val movie = intent.getParcelableExtra<Movie>("movie")
        populateUI(movie)

        val backButton = findViewById<ImageView>(R.id.back)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        movieReference = database.getReference("Reviews")

        val heartImg = findViewById<ImageView>(R.id.save)
        //Set a click listener when save is pressed
        heartImg.setOnClickListener {
            // Inflate the dialog layout
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rate_review, null)

            // Initialize views from the inflated dialog view
            val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
            val reviewEditText = dialogView.findViewById<EditText>(R.id.reviewEditText)
            val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

            // Create an AlertDialog
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Rate and Review")
                .create()

            // Set a click listener for the Save Button
            saveButton.setOnClickListener {
                val rating = ratingBar.rating
                val review = reviewEditText.text.toString()

                // Check if rating or review is empty
                if (rating == 0f || review.isEmpty()) {
                    Toast.makeText(this, "Please enter both rating and review.", Toast.LENGTH_SHORT).show()
                } else {
                    // Check if the movie object is not null
                    movie?.let { movie ->
                        // Create a new review object
                        val newReview = movie.title?.let { it1 ->
                            WatchlistItem("",it1, rating, review, movie.poster)
                        }
                        // Save the review to Firebase with a push key
                        val reviewKey = movieReference.push().key
                        // Save the review to Firebase
                        reviewKey?.let { key ->
                            newReview?.key = key
                            movieReference.child(key).setValue(newReview)
                            alertDialog.dismiss()

                            val intent = Intent(this, WatchedListActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
                // Dismiss the dialog
                alertDialog.dismiss()
            }
            cancelButton.setOnClickListener{
                // Dismiss the dialog
                alertDialog.dismiss()
            }
            alertDialog.show()
        }
    }

    // For movie details
    private fun populateUI(movie: Movie?) {
        // Check if the movie object is not null
        if (movie != null) {
            val movieNameTxt = findViewById<TextView>(R.id.movieNameTxt)
            val movieRateTxt = findViewById<TextView>(R.id.movieRateTxt)
            val movieDateTxt = findViewById<TextView>(R.id.movieDateTxt)
            val posterNormalImg = findViewById<ImageView>(R.id.posterNormalImg)
            val posterBigImg = findViewById<ImageView>(R.id.posterBigImg)
            val movieDetail = findViewById<TextView>(R.id.movieDetailInfo)
            val numbers = findViewById<TextView>(R.id.movieTimeTxt)

            movieNameTxt.text = movie.title
            movieRateTxt.text = movie.vote
            movieDateTxt.text = movie.date
            movieDetail.text = movie.detail
            numbers.text = movie.numbers


            // Load the movie poster image using Glide
            val IMAGE_BASE = "https://image.tmdb.org/t/p/w500/"
            Glide.with(this).load(IMAGE_BASE + movie.poster).into(posterNormalImg)
            Glide.with(this).load(IMAGE_BASE + movie.poster).into(posterBigImg)
        }
    }
}