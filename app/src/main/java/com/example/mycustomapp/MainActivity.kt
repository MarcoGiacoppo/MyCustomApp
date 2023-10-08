package com.example.mycustomapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycustomapp.adapters.MovieAdapter
import com.example.mycustomapp.models.Movie
import com.example.mycustomapp.models.MovieResponse
import com.example.mycustomapp.services.MovieApiInterface
import com.example.mycustomapp.services.MovieApiInterface2
import com.example.mycustomapp.services.MovieApiInterface3
import com.example.mycustomapp.services.MovieApiService
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), MovieAdapter.OnItemClickListener {

    private lateinit var rvMoviesList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvMoviesList = findViewById(R.id.rv_movies_list)
        rvMoviesList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvMoviesList.setHasFixedSize(true)

        getMovieData { movies: List<Movie> ->
            rvMoviesList.adapter = MovieAdapter(movies, this)
        }

        // Back button
        val backButton = findViewById<ImageView>(R.id.backBtn)
        backButton.setOnClickListener {
            Toast.makeText(this, "Hope to see you soon again!", Toast.LENGTH_SHORT).show()
            logoutUser()
        }

        // Watchlist button
        val watchedList = findViewById<Button>(R.id.listBtn)
        watchedList.setOnClickListener {
            val intent = Intent(this, WatchedListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onItemClick(movie: Movie) {
        // Create an intent to open the DetailActivity
        val intent = Intent(this, DetailActivity::class.java)

        // Pass the selected movie's details to the DetailActivity
        intent.putExtra("movie", movie)
        // Pass the movie_id to be put inside another api and watch trailer
        intent.putExtra("movie_id", movie.id)

        // Start the detail activity
        startActivity(intent)
    }

    private fun getMovieData(callback: (List<Movie>) -> Unit) {
        val apiService = MovieApiService.getInstance().create(MovieApiInterface::class.java)
        val apiService2 = MovieApiService.getInstance().create(MovieApiInterface2::class.java)
        val apiService3 = MovieApiService.getInstance().create(MovieApiInterface3::class.java)
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loading1)


        // Show the progress bar
        loadingProgressBar.visibility = View.VISIBLE

        // Make the first API request (page 1)
        apiService.getMovieList().enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (response.isSuccessful) {
                    val movies = response.body()?.movies ?: emptyList()

                    // After receiving data from the first request, make the second API request (page 2)
                    apiService2.getMovieList().enqueue(object : Callback<MovieResponse> {
                        override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                            if (response.isSuccessful) {
                                val moviesPage2 = response.body()?.movies ?: emptyList()

                                apiService3.getMovieList().enqueue(object : Callback<MovieResponse> {
                                    override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                                        if (response.isSuccessful) {
                                            val moviesPage3 = response.body()?.movies ?: emptyList()

                                            // Combine movies from both requests
                                            val allMovies = movies + moviesPage2 + moviesPage3

                                            // Call the callback with the combined list
                                            callback(allMovies)

                                            // Hide the loading indicator once the data has been loaded
                                            loadingProgressBar.visibility = View.GONE
                                            rvMoviesList.visibility = View.VISIBLE


                                        } else {
                                            // Handle HTTP error for the third API request
                                            Toast.makeText(this@MainActivity, "Failed to fetch data (page 3)", Toast.LENGTH_SHORT).show()
                                            // Hide the loading indicator in case of an error
                                            loadingProgressBar.visibility = View.GONE
                                        }
                                    }
                                    override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                                        // Handle network or other failures for the third API request
                                        Toast.makeText(this@MainActivity, "Network error (page 3)", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            } else {
                                // Handle HTTP error for the second API request
                                Toast.makeText(this@MainActivity, "Failed to fetch data (page 2)", Toast.LENGTH_SHORT).show()
                                // Hide the loading indicator in case of an error
                                loadingProgressBar.visibility = View.GONE
                            }
                        }

                        override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                            // Handle network or other failures for the second API request
                            Toast.makeText(this@MainActivity, "Network error (page 2)", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    // Handle HTTP error for the first API request
                    Toast.makeText(this@MainActivity, "Failed to fetch data (page 1)", Toast.LENGTH_SHORT).show()
                    // Hide the loading indicator in case of an error
                    loadingProgressBar.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                // Handle network or other failures for the first API request
                Toast.makeText(this@MainActivity, "Network error (page 1)", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // After users presses on the sign out button, they won't be able to edit data
    private fun logoutUser() {
        // Sign out the user using Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        // After logging out, navigate to the login screen
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
