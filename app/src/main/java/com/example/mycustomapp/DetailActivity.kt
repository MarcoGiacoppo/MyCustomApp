package com.example.mycustomapp

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.example.mycustomapp.models.Movie
import com.example.mycustomapp.models.TVShow
import com.example.mycustomapp.models.WatchlistItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject


class DetailActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var movieReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_detail)

        // Retrieve the movie details from the Intent extras
        val movie = intent.getParcelableExtra<Movie>("movie")
        val movieId = intent.getIntExtra("movie_id", 0)

        // Retrieve the TV Show details from the intent
        val tvShow = intent.getParcelableExtra<TVShow>("tvShow")
        val tvShowId = intent.getIntExtra("tv_id", 0)

        val searchQuery = intent.getStringExtra("search_query")

        populateUI(movie)
        populateUI(tvShow)

        val backButton = findViewById<ImageView>(R.id.back)
        val source = intent.getStringExtra("source")

        backButton.setOnClickListener {
            val intent = when (source) {
                "recommendation" -> Intent(this, DiscoverActivity::class.java)
                "main" -> Intent(this, MainActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }

            // Include the search query if available
            if (searchQuery != null) {
                intent.putExtra("search_query", searchQuery)
            }
            startActivity(intent)
        }

        val watchTrailer = findViewById<TextView>(R.id.trailer)
        watchTrailer.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        watchTrailer.isEnabled = false

        val loadingIndicator = findViewById<TextView>(R.id.trailer_loading)
        loadingIndicator.text = "Fetching trailer..."

        GlobalScope.launch(Dispatchers.IO) {
            val movieYouTubeKey = fetchYouTubeKey(movieId, "movie")
            val tvShowYouTubeKey = fetchYouTubeKey(tvShowId, "tvShow")

            withContext(Dispatchers.Main) {
                if (movieYouTubeKey != null || tvShowYouTubeKey != null) {
                    watchTrailer.tag = movieYouTubeKey ?: tvShowYouTubeKey
                    watchTrailer.isEnabled = true
                } else {
                    watchTrailer.text = "Official Trailer Not Available"
                    watchTrailer.isEnabled = false
                    watchTrailer.setTextColor(resources.getColor(R.color.greyText))
                }
                loadingIndicator.visibility = View.GONE
            }
        }

        // Set an OnClickListener to open the YouTube video when the "Watch Trailer" TextView is clicked
        watchTrailer.setOnClickListener {
            val youTubeKey = watchTrailer.tag as? String
            if (!youTubeKey.isNullOrEmpty()) {
                val youtubeUrl = "https://www.youtube.com/watch?v=$youTubeKey"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show()
            }
        }
        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        movieReference = database.getReference("Reviews")

        val heartImg = findViewById<ImageView>(R.id.save)
        // Set a click listener when the heart image is pressed
        heartImg.setOnClickListener {
            // Inflate the dialog layout
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rate_review, null)
            // Create an AlertDialog
            val alertDialog = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setView(dialogView)
                .setTitle("Rate and Review")
                .create()

            // Initialize views from the inflated dialog view
            val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
            val reviewEditText = dialogView.findViewById<EditText>(R.id.reviewEditText)
            val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

            // Set a click listener for the Save Button
            saveButton.setOnClickListener {
                val rating = ratingBar.rating
                val review = reviewEditText.text.toString()

                // Check if rating or review is empty
                if (rating == 0f || review.isEmpty()) {
                    Toast.makeText(this, "Please enter both rating and review.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Saved to watched list.", Toast.LENGTH_SHORT).show()
                    // Check if the movie object is not null
                    movie?.let { movie ->
                        // Get the currently authenticated user
                        val user = FirebaseAuth.getInstance().currentUser // Get the user
                        if (user!= null) {
                            val userId = user.uid

                            // Save the review to Firebase with a push key
                            val reviewKey = movieReference.push().key
                            // Save the review to Firebase
                            reviewKey?.let { key ->
                                val newReview = WatchlistItem(
                                    key, userId, movie.title ?: "", rating, review, movie.poster, movieId)
                                movieReference.child(key).setValue(newReview)
                                alertDialog.dismiss()

                                val intent = Intent(this, WatchedListActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
            cancelButton.setOnClickListener {
                // Dismiss the dialog
                alertDialog.dismiss()
            }
            alertDialog.show()
        }
    }

    private suspend fun fetchYouTubeKey(Id: Int, type: String): String? {
        val apiKey = "381e5879afdcdcba913bc1f839a6f004"
        val baseUrl = when (type) {
            "movie" -> "https://api.themoviedb.org/3/movie/"
            "tvShow" -> "https://api.themoviedb.org/3/tv/"
            else -> return null
        }

        val url = "$baseUrl$Id/videos?api_key=$apiKey"

        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                val json = JSONObject(responseBody)

                val resultsArray = json.getJSONArray("results")
                for (i in 0 until resultsArray.length()) {
                    val videoObject = resultsArray.getJSONObject(i)
                    val name = videoObject.getString("name")

                    if (name == "Official Trailer") {
                        return@withContext videoObject.getString("key")
                    }
                }
            }
            return@withContext null
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
    // For TV Show details
    private fun populateUI(tvShow: TVShow?) {
        // Check if the tv show object is not null
        if (tvShow != null) {
            val movieNameTxt = findViewById<TextView>(R.id.movieNameTxt)
            val movieRateTxt = findViewById<TextView>(R.id.movieRateTxt)
            val movieDateTxt = findViewById<TextView>(R.id.movieDateTxt)
            val posterNormalImg = findViewById<ImageView>(R.id.posterNormalImg)
            val posterBigImg = findViewById<ImageView>(R.id.posterBigImg)
            val movieDetail = findViewById<TextView>(R.id.movieDetailInfo)
            val numbers = findViewById<TextView>(R.id.movieTimeTxt)

            movieNameTxt.text = tvShow.name
            movieRateTxt.text = tvShow.vote
            movieDateTxt.text = tvShow.date
            movieDetail.text = tvShow.detail
            numbers.text = tvShow.numbers

            // Load the movie poster image using Glide
            val IMAGE_BASE = "https://image.tmdb.org/t/p/w500/"
            Glide.with(this).load(IMAGE_BASE + tvShow.poster).into(posterNormalImg)
            Glide.with(this).load(IMAGE_BASE + tvShow.poster).into(posterBigImg)
        }
    }
}
