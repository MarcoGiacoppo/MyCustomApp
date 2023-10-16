package com.example.mycustomapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycustomapp.adapters.MovieAdapter
import com.example.mycustomapp.models.Movie
import com.example.mycustomapp.services.RecommendationMovie
import com.example.mycustomapp.services.TopRatedMovie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.CoroutineContext

class RecommendationActivity : AppCompatActivity(), MovieAdapter.OnItemClickListener, CoroutineScope {

    private lateinit var rvRecommendedMovies: RecyclerView
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendation)

        rvRecommendedMovies = findViewById(R.id.recommendationRV)
        rvRecommendedMovies.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        job = Job()

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Fetch and populate recommended movies
        fetchRecommendedMovies()

        // Fetch and populate Top Rated movies
        fetchTopRatedMovies()
    }

    override fun onItemClick(movie: Movie) {
        // Create an intent to open the DetailActivity
        val intent = Intent(this, DetailActivity::class.java)

        // Pass the selected movie's details to the DetailActivity
        intent.putExtra("movie", movie)
        // Pass the movie_id to be put inside another API and watch the trailer
        intent.putExtra("movie_id", movie.id)

        // Start the detail activity
        startActivity(intent)
    }

    private fun fetchRecommendedMovies() {
        val apiKey = "381e5879afdcdcba913bc1f839a6f004"
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val progressBar = findViewById<ProgressBar>(R.id.progressrec)

            if (userId != null) {
                // Query the Firebase database for reviews with userRating = 5
                val reviewsReference = FirebaseDatabase.getInstance().getReference("Reviews")
                val fiveStarReviewQuery = reviewsReference
                    .orderByChild("userId")
                    .equalTo(userId)

                fiveStarReviewQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val recommendedMovieIds = mutableListOf<Int>()

                        for (reviewSnapshot in snapshot.children) {
                            // Check the rating field of each review
                            val rating =
                                reviewSnapshot.child("userRating").getValue(Int::class.java)
                            if (rating != null && rating == 5) {
                                // Extract the movie_id if the rating is 5
                                val movieId = reviewSnapshot.child("id").getValue(Int::class.java)
                                if (movieId != null) {
                                    recommendedMovieIds.add(movieId)
                                }
                            }
                        }
                        if (recommendedMovieIds.isEmpty()) {
                            progressBar.visibility = View.VISIBLE
                            // Show a Toast message if there are no 5-star ratings
                            Toast.makeText(this@RecommendationActivity, "You need to have at least one 5-star rating to get recommendations.", Toast.LENGTH_LONG).show()
                        } else {
                            launch {
                                progressBar.visibility = View.VISIBLE
                                val recommendedMovies =
                                    fetchRecommendations(recommendedMovieIds, apiKey)
                                rvRecommendedMovies.adapter =
                                    MovieAdapter(recommendedMovies, this@RecommendationActivity)
                                rvRecommendedMovies.adapter?.notifyDataSetChanged()

                                progressBar.visibility = View.GONE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    // Handle errors
                    }
                })
            }
        }

        private suspend fun fetchRecommendations(movieIds: List<Int>, apiKey: String): List<Movie> {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(RecommendationMovie::class.java)

            // Initialize an empty list to store recommended movies
            val recommendedMovies = mutableListOf<Movie>()

            for (movieId in movieIds) {
                val recommendedMoviesResponse = withContext(Dispatchers.IO) {
                    apiService.getMovies(movieId , apiKey).execute()
                }

                if (recommendedMoviesResponse.isSuccessful) {
                    recommendedMovies.addAll(recommendedMoviesResponse.body()?.movies ?: emptyList())

                }
            }
            return recommendedMovies
        }

        private fun fetchTopRatedMovies() {
            val apiKey = "381e5879afdcdcba913bc1f839a6f004"
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)

            launch {
                progressBar.visibility = View.VISIBLE

                val topRatedMovies = fetchTopRatedMovies(apiKey)

                if (topRatedMovies.isNotEmpty()) {
                    val topRatedRecyclerView = findViewById<RecyclerView>(R.id.topratedRV)

                    topRatedRecyclerView.layoutManager = LinearLayoutManager(this@RecommendationActivity, LinearLayoutManager.HORIZONTAL, false)
                    topRatedRecyclerView.adapter = MovieAdapter(topRatedMovies, this@RecommendationActivity)
                    progressBar.visibility = View.GONE
                }
            }
        }

        private suspend fun fetchTopRatedMovies(apiKey: String): List<Movie> {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(TopRatedMovie::class.java)

            val topRatedMovieResponse = withContext(Dispatchers.IO) {
                apiService.getTopRatedMovies(apiKey).execute()
            }

            return if (topRatedMovieResponse.isSuccessful) {
                topRatedMovieResponse.body()?.movies ?: emptyList()
            } else {
                emptyList()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
