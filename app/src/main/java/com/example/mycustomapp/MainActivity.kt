package com.example.mycustomapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), MovieAdapter.OnItemClickListener{

    private lateinit var rvMoviesList: RecyclerView

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

        val toggleSwitch = findViewById<Switch>(R.id.toggleSwitch)
        val listButton = findViewById<Button>(R.id.listBtn)

        // Set the initial click behaviour of the button
        listButton.setOnClickListener {
            val intent = Intent(this, WatchedListActivity::class.java)
            startActivity(intent)
        }

        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listButton.text = "Recommendation"
                listButton.setOnClickListener {
                    val intent = Intent(this, RecommendationActivity::class.java)
                    startActivity(intent)
                }
            } else {
                listButton.text = "My Watched List"
                listButton.setOnClickListener {
                    val intent = Intent(this, WatchedListActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        getMovieData { movies: List<Movie> ->
            rvMoviesList.adapter = MovieAdapter(movies, this)
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

    private fun getMovieData(callback: (List<Movie>) -> Unit) {
        val apiService = MovieApiService.getInstance().create(MovieApiInterface::class.java)
        val apiService2 = MovieApiService.getInstance().create(MovieApiInterface2::class.java)
        val apiService3 = MovieApiService.getInstance().create(MovieApiInterface3::class.java)
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loading1)

        // Show the progress bar
        loadingProgressBar.visibility = View.VISIBLE

        // Use Kotlin Coroutines to perform asynchronous network requests
        // Launch a new coroutine to fetch data
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response1 = apiService.getMovieList().execute()
                val response2 = apiService2.getMovieList().execute()
                val response3 = apiService3.getMovieList().execute()

                if (response1.isSuccessful && response2.isSuccessful && response3.isSuccessful) {
                    val movies = (response1.body()?.movies ?: emptyList()) +
                            (response2.body()?.movies ?: emptyList()) +
                            (response3.body()?.movies ?: emptyList())

                    // Switch to the main thread to update the UI
                    withContext(Dispatchers.Main) {
                        callback(movies)
                        // Hide the loading indicator
                        loadingProgressBar.visibility = View.GONE
                    }
                } else {
                    // Handle network request errors
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                        // Hide the loading indicator in case of an error
                        loadingProgressBar.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            }
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
}
