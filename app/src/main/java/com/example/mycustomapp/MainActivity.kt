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
import com.example.mycustomapp.adapters.TVShowAdapter
import com.example.mycustomapp.models.Movie
import com.example.mycustomapp.models.TVShow
import com.example.mycustomapp.services.MovieApiInterface
import com.example.mycustomapp.services.MovieApiInterface2
import com.example.mycustomapp.services.MovieApiInterface3
import com.example.mycustomapp.services.MovieApiService
import com.example.mycustomapp.services.TopRatedMovieInterface1
import com.example.mycustomapp.services.TopRatedMovieInterface2
import com.example.mycustomapp.services.popularTVshowInterface1
import com.example.mycustomapp.services.popularTVshowInterface2
import com.example.mycustomapp.services.topRatedTVshowInterface1
import com.example.mycustomapp.services.topRatedTVshowInterface2
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

class MainActivity : AppCompatActivity(), MovieAdapter.OnItemClickListener, TVShowAdapter.OnItemClickListener{

    private lateinit var rvMoviesList: RecyclerView
    private lateinit var rvMoviesList2: RecyclerView

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        rvMoviesList = findViewById(R.id.rv_movies_list)
        rvMoviesList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvMoviesList.setHasFixedSize(true)

        rvMoviesList2 = findViewById(R.id.rv_tvshow)
        rvMoviesList2.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvMoviesList2.setHasFixedSize(true)

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

        // For the movie part
        val popularMoviesTextView = findViewById<TextView>(R.id.popularText)
        val topRatedMoviesTextView = findViewById<TextView>(R.id.topMoviesText)

        popularMoviesTextView.setOnClickListener {
            saveUserChoice("Popular Movies")
            popularMoviesTextView.setTextColor(resources.getColor(R.color.yellow))
            topRatedMoviesTextView.setTextColor(resources.getColor(R.color.greyText))
            topRatedMoviesTextView.isEnabled = true
            popularMoviesTextView.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val movies = getMovieData("Popular Movies")
                rvMoviesList.adapter = MovieAdapter(movies, this@MainActivity)
            }
        }

        topRatedMoviesTextView.setOnClickListener {
            saveUserChoice("Top Rated Movies")
            topRatedMoviesTextView.setTextColor(resources.getColor(R.color.yellow))
            popularMoviesTextView.setTextColor(resources.getColor(R.color.greyText))
            popularMoviesTextView.isEnabled = true
            topRatedMoviesTextView.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val movies = getMovieData("Top Rated Movies")
                rvMoviesList.adapter = MovieAdapter(movies, this@MainActivity)
            }
        }

        // Check user's choice and load data accordingly
        val userChoice = getUserChoice()
        if (userChoice == "Popular Movies") {
            popularMoviesTextView.callOnClick()
        } else {
            topRatedMoviesTextView.callOnClick()
        }

        // For the Tv Show part
        val popularTvTextView = findViewById<TextView>(R.id.popularTvshow)
        val topRatedTvTextView = findViewById<TextView>(R.id.topRatedTvshow)

        popularTvTextView.setOnClickListener {
            saveUserChoiceTV("Popular TV Show")
            popularTvTextView.setTextColor(resources.getColor(R.color.yellow))
            topRatedTvTextView.setTextColor(resources.getColor(R.color.greyText))
            topRatedTvTextView.isEnabled = true
            popularTvTextView.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val tvShow = getTVData("Popular TV Show")
                rvMoviesList2.adapter = TVShowAdapter(tvShow, this@MainActivity)
            }
        }

        topRatedTvTextView.setOnClickListener {
            saveUserChoiceTV("Top Rated TV Show")
            topRatedTvTextView.setTextColor(resources.getColor(R.color.yellow))
            popularTvTextView.setTextColor(resources.getColor(R.color.greyText))
            popularTvTextView.isEnabled = true
            topRatedTvTextView.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val tvShow = getTVData("Top Rated TV Show")
                rvMoviesList2.adapter = TVShowAdapter(tvShow, this@MainActivity)
            }
        }

        // Check user's choice and load data accordingly
        val userChoiceTV = getUserChoiceTV()
        if (userChoiceTV == "Popular TV Show") {
            popularTvTextView.callOnClick()
        } else {
            topRatedTvTextView.callOnClick()
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

    override fun onItemClick(tvShow: TVShow) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("tvshow", tvShow)
        intent.putExtra("tv_id", tvShow.id)
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
            "Top Rated Movies" -> {
                val deferred1 = coroutineScope.async {
                    val topRatedMovieInterface1 = MovieApiService.getInstance().create(TopRatedMovieInterface1::class.java)
                    val response1 = topRatedMovieInterface1.getMovieList().execute()
                    if (response1.isSuccessful) {
                        response1.body()?.movies ?: emptyList()
                    } else {
                        emptyList()
                    }
                }
                val deferred2 = coroutineScope.async {
                    val topRatedMovieInterface2 = MovieApiService.getInstance().create(TopRatedMovieInterface2::class.java)
                    val response2 = topRatedMovieInterface2.getMovieList().execute()
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

    private suspend fun getTVData(dataType: String): (List<TVShow>) {
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loadingTV)
        // Show the progress bar
        loadingProgressBar.visibility = View.VISIBLE

        val tvShow = mutableListOf<TVShow>()

        val coroutineScope = CoroutineScope(Dispatchers.IO)

        when (dataType) {
            "Popular TV Show" -> {
                val deferred1 = coroutineScope.async {
                    val apiService1 = MovieApiService.getInstance().create(popularTVshowInterface1::class.java)
                    val response1 = apiService1.getTVlist().execute()
                    if (response1.isSuccessful) {
                        response1.body()?.tvShows ?: emptyList()
                    } else {
                        emptyList()
                    }
                }

                val deferred2 = coroutineScope.async {
                    val apiService2 = MovieApiService.getInstance().create(popularTVshowInterface2::class.java)
                    val response2 = apiService2.getTVlist().execute()
                    if (response2.isSuccessful) {
                        response2.body()?.tvShows ?: emptyList()
                    } else {
                        emptyList()
                    }
                }
                // Wait for all deferred tasks to finish
                tvShow.addAll(deferred1.await())
                tvShow.addAll(deferred2.await())
            }
            "Top Rated TV Show" -> {
                val deferred1 = coroutineScope.async {
                    val topRatedTVshow = MovieApiService.getInstance().create(topRatedTVshowInterface1::class.java)
                    val response1 = topRatedTVshow.getTVlist().execute()
                    if (response1.isSuccessful) {
                        response1.body()?.tvShows ?: emptyList()
                    } else {
                        emptyList()
                    }
                }
                val deferred2 = coroutineScope.async {
                    val topRatedTVshow2 = MovieApiService.getInstance().create(topRatedTVshowInterface2::class.java)
                    val response2 = topRatedTVshow2.getTVlist().execute()
                    if (response2.isSuccessful) {
                        response2.body()?.tvShows ?: emptyList()
                    } else {
                        emptyList()
                    }
                }

                // Wait for all deferred tasks to finish
                tvShow.addAll(deferred1.await())
                tvShow.addAll(deferred2.await())
            }
            else -> throw IllegalArgumentException("Invalid Data Type")
        }
        // Switch to the main thread to update the UI
        withContext(Dispatchers.Main) {
            loadingProgressBar.visibility = View.GONE
        }
        return tvShow
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

    private fun saveUserChoiceTV(choice: String) {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userChoiceTV", choice)
        editor.apply()
    }
    private fun getUserChoice(): String? {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("userChoice", null)
    }

    private fun getUserChoiceTV(): String? {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("userChoiceTV", null)
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
