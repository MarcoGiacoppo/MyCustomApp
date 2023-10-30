package com.example.mycustomapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycustomapp.adapters.MovieAdapter
import com.example.mycustomapp.models.Movie
import com.example.mycustomapp.services.PlayingNowMovies
import com.example.mycustomapp.services.RecommendationMovie
import com.example.mycustomapp.services.UpcomingMovie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
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
        fetchPlayingNowMovies()

        // Fetch and populate Upcoming movies
        fetchUpcomingMovies()

        // Search bar
        val searchContainer = findViewById<LinearLayout>(R.id.searchContainer)
        val searchView = findViewById<SearchView>(R.id.searchView)

        searchContainer.setOnClickListener {
            // Programmatically expand the SearchView when the container is clicked
            searchView.isIconified = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    // Clear focus to hide the keyboard
                    searchView.clearFocus()

                    // Perform the movie search
                    performMovieSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun performMovieSearch(query: String) {
        val apiKey = "381e5879afdcdcba913bc1f839a6f004"
        val baseUrl = "https://api.themoviedb.org/3/search/multi"

        val client = OkHttpClient()

        val url = HttpUrl.parse(baseUrl)?.newBuilder()
            ?.addQueryParameter("api_key", apiKey)
            ?.addQueryParameter("query", query)
            ?.build()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network request failure
                Log.e("NetworkRequest", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Log.d("NetworkRequest", "Response successful. Body: $responseBody")
                    val json = JSONObject(responseBody)

                    // Process the JSON response
                    val results = json.getJSONArray("results")
                    // Handle the results as needed

                } else {
                    // Handle non successful response
                    Log.e("NetworkRequest", "Response not successful. Code: ${response.code()}")
                }
            }
        })
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
        val progressBar = findViewById<ProgressBar>(R.id.progressRec)
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
                        Toast.makeText(
                            this@RecommendationActivity,
                            "You need to have at least one 5-star rating to get recommendations.",
                            Toast.LENGTH_LONG
                        ).show()
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

    private fun fetchPlayingNowMovies() {
            val apiKey = "381e5879afdcdcba913bc1f839a6f004"
            val progressBar = findViewById<ProgressBar>(R.id.progressPlayingNow)

            launch {
                progressBar.visibility = View.VISIBLE

                val nowPlayingMovies = fetchPlayingNowMovies(apiKey)

                if (nowPlayingMovies.isNotEmpty()) {
                    val playingNowRecyclerView = findViewById<RecyclerView>(R.id.playingNowRV)

                    playingNowRecyclerView.layoutManager = LinearLayoutManager(this@RecommendationActivity, LinearLayoutManager.HORIZONTAL, false)
                    playingNowRecyclerView.adapter = MovieAdapter(nowPlayingMovies, this@RecommendationActivity)
                    progressBar.visibility = View.GONE
                }
            }
        }

    private suspend fun fetchPlayingNowMovies(apiKey: String): List<Movie> {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(PlayingNowMovies::class.java)

            val playingNowMoviesResponse = withContext(Dispatchers.IO) {
                apiService.getNowPlayingMovies(apiKey).execute()
            }

            return if (playingNowMoviesResponse.isSuccessful) {
                playingNowMoviesResponse.body()?.movies ?: emptyList()
            } else {
                emptyList()
            }
        }

    private fun fetchUpcomingMovies() {
        val apiKey = "381e5879afdcdcba913bc1f839a6f004"
        val progressBar = findViewById<ProgressBar>(R.id.progressUpcoming)

        launch {
            progressBar.visibility = View.VISIBLE

            val upcomingMovies = fetchUpcomingMovies(apiKey)

            if (upcomingMovies.isNotEmpty()) {
                val upcomingRecyclerView = findViewById<RecyclerView>(R.id.upcomingRV)

                upcomingRecyclerView.layoutManager = LinearLayoutManager(this@RecommendationActivity, LinearLayoutManager.HORIZONTAL, false)
                upcomingRecyclerView.adapter = MovieAdapter(upcomingMovies, this@RecommendationActivity)
                progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun fetchUpcomingMovies(apiKey: String): List<Movie> {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(UpcomingMovie::class.java)

        val upcomingMovieResponse = withContext(Dispatchers.IO) {
            apiService.getUpcomingMovies(apiKey).execute()
        }

        return if (upcomingMovieResponse.isSuccessful) {
            upcomingMovieResponse.body()?.movies ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
