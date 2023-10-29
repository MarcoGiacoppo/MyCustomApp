package com.example.mycustomapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycustomapp.adapters.MovieAdapter
import com.example.mycustomapp.models.Movie
import com.example.mycustomapp.services.MovieApiInterface
import com.example.mycustomapp.services.MovieApiInterface2
import com.example.mycustomapp.services.MovieApiInterface3
import com.example.mycustomapp.services.MovieApiService
import com.example.mycustomapp.services.PlayingNowInterface1
import com.example.mycustomapp.services.PlayingNowInterface2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), MovieAdapter.OnItemClickListener{

    private lateinit var rvMoviesList: RecyclerView
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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

        // Testing new feature
        val popularMoviesTextView = findViewById<TextView>(R.id.popularText)
        val playingNowTextView = findViewById<TextView>(R.id.playingText)

        popularMoviesTextView.setOnClickListener {
            saveUserChoice("Popular Movies")
            popularMoviesTextView.setTextColor(resources.getColor(R.color.yellow))
            playingNowTextView.setTextColor(resources.getColor(R.color.greyText))
            playingNowTextView.isEnabled = true
            popularMoviesTextView.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val movies = getMovieData("Popular Movies")
                rvMoviesList.adapter = MovieAdapter(movies, this@MainActivity)
            }
        }

        playingNowTextView.setOnClickListener {
            saveUserChoice("Playing Now")
            playingNowTextView.setTextColor(resources.getColor(R.color.yellow))
            popularMoviesTextView.setTextColor(resources.getColor(R.color.greyText))
            popularMoviesTextView.isEnabled = true
            playingNowTextView.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val movies = getMovieData("Playing Now")
                rvMoviesList.adapter = MovieAdapter(movies, this@MainActivity)
            }
        }

        // Check user's choice and load data accordingly
        val userChoice = getUserChoice()
        if (userChoice == "Popular Movies") {
            popularMoviesTextView.callOnClick()
        } else {
            playingNowTextView.callOnClick()
        }

        // Navbar
        val homeButton = findViewById<ImageView>(R.id.homeImage)
        val movieButton = findViewById<ImageView>(R.id.moviesImage)
        val listButtons = findViewById<ImageView>(R.id.listImage)

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        movieButton.setOnClickListener {
            val intent = Intent(this, RecommendationActivity::class.java)
            startActivity(intent)
        }

        listButtons.setOnClickListener {
            val intent = Intent(this, WatchedListActivity::class.java)
            startActivity(intent)
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

    private suspend fun getMovieData(dataType: String): (List<Movie>) {
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loading1)
        // Show the progress bar
        loadingProgressBar.visibility = View.VISIBLE

        val movies = mutableListOf<Movie>()

        val coroutineScope = CoroutineScope(Dispatchers.IO)

        when (dataType) {
            "Popular Movies" -> {
                val deferred1 = coroutineScope.async {
                    val apiService1 = MovieApiService.getInstance().create(MovieApiInterface::class.java)
                    val response1 = apiService1.getMovieList().execute()
                    if (response1.isSuccessful) {
                        response1.body()?.movies ?: emptyList()
                    } else {
                        emptyList()
                    }
                }
                val deferred2 = coroutineScope.async {
                    val apiService2 = MovieApiService.getInstance().create(MovieApiInterface2::class.java)
                    val response2 = apiService2.getMovieList().execute()
                    if (response2.isSuccessful) {
                        response2.body()?.movies ?: emptyList()
                    } else {
                        emptyList()
                    }
                }
                val deferred3 = coroutineScope.async {
                    val apiService3 = MovieApiService.getInstance().create(MovieApiInterface3::class.java)
                    val response3 = apiService3.getMovieList().execute()
                    if (response3.isSuccessful) {
                        response3.body()?.movies ?: emptyList()
                    } else {
                        emptyList()
                    }
                }
                // Wait for all deferred tasks to finish
                movies.addAll(deferred1.await())
                movies.addAll(deferred2.await())
                movies.addAll(deferred3.await())
            }
            "Playing Now" -> {
                val deferred1 = coroutineScope.async {
                    val playingNowInterface1 = MovieApiService.getInstance().create(PlayingNowInterface1::class.java)
                    val response1 = playingNowInterface1.getMovieList().execute()
                    if (response1.isSuccessful) {
                        response1.body()?.movies ?: emptyList()
                    } else {
                        emptyList()
                    }
                }
                val deferred2 = coroutineScope.async {
                    val playingNowInterface2 = MovieApiService.getInstance().create(PlayingNowInterface2::class.java)
                    val response2 = playingNowInterface2.getMovieList().execute()
                    if (response2.isSuccessful){
                        response2.body()?.movies?: emptyList()
                    } else {
                        emptyList()
                    }
                }
                // Wait for all deferred tasks to finish
                movies.addAll(deferred1.await())
                movies.addAll(deferred2.await())
            }
            else -> throw IllegalArgumentException("Invalid Data Type")
        }
        // Switch to the main thread to update the UI
        withContext(Dispatchers.Main) {
            loadingProgressBar.visibility = View.GONE
        }
        return movies
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

    private fun saveUserChoice(choice: String) {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userChoice", choice)
        editor.apply()
    }

    private fun getUserChoice(): String? {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("userChoice", null)
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
}
