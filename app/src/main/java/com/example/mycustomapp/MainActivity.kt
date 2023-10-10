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
import com.example.mycustomapp.services.MovieApiInterface
import com.example.mycustomapp.services.MovieApiInterface2
import com.example.mycustomapp.services.MovieApiInterface3
import com.example.mycustomapp.services.MovieApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), MovieAdapter.OnItemClickListener, CoroutineScope {

    private lateinit var rvMoviesList: RecyclerView

    // Create a coroutine job to manage the API requests
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvMoviesList = findViewById(R.id.rv_movies_list)
        rvMoviesList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvMoviesList.setHasFixedSize(true)

        // Load the user's avatar
        loadUserAvatar()

        // Back button
        val backButton = findViewById<ImageView>(R.id.backBtn)
        backButton.setOnClickListener {
            Toast.makeText(this, "Hope to see you soon again!", Toast.LENGTH_SHORT).show()
            logoutUser()
        }

        // Profile button
        val profileButton = findViewById<ImageView>(R.id.profileBtn)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Watchlist button
        val watchedList = findViewById<Button>(R.id.listBtn)
        watchedList.setOnClickListener {
            val intent = Intent(this, WatchedListActivity::class.java)
            startActivity(intent)
        }

        val loadingProgressBar = findViewById<ProgressBar>(R.id.loading1)

        launch {
            try {
                val movies = getMoviesFromApi()
                rvMoviesList.adapter = MovieAdapter(movies, this@MainActivity)
                loadingProgressBar.visibility = View.GONE
                rvMoviesList.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                loadingProgressBar.visibility = View.GONE
            }
        }
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

    private suspend fun getMoviesFromApi(): List<Movie> = withContext(Dispatchers.IO) {
        val apiService = MovieApiService.getInstance().create(MovieApiInterface::class.java)
        val apiService2 = MovieApiService.getInstance().create(MovieApiInterface2::class.java)
        val apiService3 = MovieApiService.getInstance().create(MovieApiInterface3::class.java)
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loading1)

        // Show the progress bar
        loadingProgressBar.visibility = View.VISIBLE

        // Use async to make concurrent API requests
        val moviesDeferred = async { apiService.getMovieList().execute() }
        val moviesPage2Deferred = async { apiService2.getMovieList().execute() }
        val moviesPage3Deferred = async { apiService3.getMovieList().execute() }

        val moviesResponse = moviesDeferred.await()
        val moviesPage2Response = moviesPage2Deferred.await()
        val moviesPage3Response = moviesPage3Deferred.await()

        if (moviesResponse.isSuccessful && moviesPage2Response.isSuccessful && moviesPage3Response.isSuccessful) {
            val movies = moviesResponse.body()?.movies ?: emptyList()
            val moviesPage2 = moviesPage2Response.body()?.movies ?: emptyList()
            val moviesPage3 = moviesPage3Response.body()?.movies ?: emptyList()

            return@withContext movies + moviesPage2 + moviesPage3
        } else {
            throw Exception("Failed to fetch data")
        }
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

    private fun loadUserAvatar() {
        // Get the currently logged-in user
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid

            // Retrieve the user's "avatar" field from the database
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("Users")

            usersRef.child(userId).child("avatar").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get the selected avatar identifier
                    val selectedAvatarId = dataSnapshot.getValue(String::class.java)

                    // Update the ImageView with the selected avatar
                    val profileImageView = findViewById<ImageView>(R.id.profileBtn)
                    when (selectedAvatarId) {
                        "avatar1" -> profileImageView.setImageResource(R.drawable.avatar1)
                        "avatar2" -> profileImageView.setImageResource(R.drawable.avatar2)
                        "avatar3" -> profileImageView.setImageResource(R.drawable.avatar3)
                        "avatar4" -> profileImageView.setImageResource(R.drawable.avatar4)
                        "avatar5" -> profileImageView.setImageResource(R.drawable.avatar5)
                        "avatar6" -> profileImageView.setImageResource(R.drawable.avatar6)
                        "avatar7" -> profileImageView.setImageResource(R.drawable.avatar7)
                        "avatar8" -> profileImageView.setImageResource(R.drawable.avatar8)
                        // Add more cases for additional avatar options
                        else -> profileImageView.setImageResource(R.drawable.ic_round_person_24) // Default avatar
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors here
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel the coroutine job when the activity is destroyed
    }
}
