package com.example.mycustomapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycustomapp.adapters.WatchedAdapter
import com.example.mycustomapp.databinding.ActivityWatchedListBinding
import com.example.mycustomapp.models.WatchlistItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class WatchedListActivity : AppCompatActivity(),
    WatchedAdapter.OnDeleteItemClickListener,
    WatchedAdapter.OnEditItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WatchedAdapter
    private lateinit var watchedMoviesReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWatchedListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loadingProgressBar = findViewById<ProgressBar>(R.id.loading2)

        // Show the progress bar
        loadingProgressBar.visibility = View.VISIBLE

        // Initialize the RecyclerView
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Firebase and get a reference to watched movies
        watchedMoviesReference = FirebaseDatabase.getInstance().getReference("Reviews")

        // Initialize an empty list for watched movies
        val reviewList: MutableList<WatchlistItem> = mutableListOf()

        // Create a default item click listener
        val itemClickListener = object : WatchedAdapter.OnItemClickListener {
            override fun onItemClick(watchlistItem: WatchlistItem) {
            }
        }

        // Initialize the adapter
        adapter = WatchedAdapter(reviewList, itemClickListener, this, this)

        val backBtn = findViewById<ImageView>(R.id.back)
        backBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Create a ValueEventListener to fetch data from Firebase
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the previous list
                reviewList.clear()

                // Get the currently authenticated user
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userId = user.uid

                    for (postSnapshot in dataSnapshot.children) {
                        // Deserialize the data into a WatchlistItem object
                        val watchlistItem = postSnapshot.getValue(WatchlistItem::class.java)

                        if (watchlistItem != null && watchlistItem.userId == userId) {
                            reviewList.add(watchlistItem)
                            // Hide the loading indicator once the data has been loaded
                            loadingProgressBar.visibility = View.GONE
                        }
                    }
                    // Set the adapter for the RecyclerView
                    recyclerView.adapter = adapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }

        // Add the ValueEventListener to the watchedMoviesReference
        watchedMoviesReference.addValueEventListener(valueEventListener)
    }

    override fun onDeleteItemClick(watchlistItem: WatchlistItem) {
        val watchlistItemKey = watchlistItem.key
        if (watchlistItemKey != null) {
            val itemReference = watchedMoviesReference.child(watchlistItemKey)
            itemReference.removeValue() // Remove the item from Firebase
        }
    }

    // Function for edit item
    override fun onEditItemClick(watchlistItem: WatchlistItem) {
        val watchlistItemKey = watchlistItem.key

        if (watchlistItemKey != null) {
            val itemReference = watchedMoviesReference.child(watchlistItemKey)

            // Show a dialog to edit userRating and userReview
            val editDialogView = LayoutInflater.from(this).inflate(R.layout.edit_dialog, null)
            val editDialog = AlertDialog.Builder(this)
                .setTitle("Edit Item")
                .setView(editDialogView)
                .setPositiveButton("Save") { dialog, _ ->
                    val newRatingBar = editDialogView.findViewById<RatingBar>(R.id.editMovieRatingBar)
                    val newReviewEditText = editDialogView.findViewById<EditText>(R.id.editMovieReview)

                    // Get the new userRating and userReview from the dialog view
                    val newRating = newRatingBar.rating
                    val newReview = newReviewEditText.text.toString()

                    // Update the Firebase data
                    itemReference.child("userRating").setValue(newRating)
                    itemReference.child("userReview").setValue(newReview)

                    dialog.dismiss()

                    // Show toast if success
                    Toast.makeText(this@WatchedListActivity, "Data edited successfully", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()

            // Fetch the saved data
            itemReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userRating =
                            dataSnapshot.child("userRating").getValue(Float::class.java)
                        val userReview =
                            dataSnapshot.child("userReview").getValue(String::class.java)

                        // Pre-fill the dialog fields with existing data
                        val newRatingBar =
                            editDialogView.findViewById<RatingBar>(R.id.editMovieRatingBar)
                        val newReviewEditText =
                            editDialogView.findViewById<EditText>(R.id.editMovieReview)

                        newRatingBar.rating = userRating ?: 0f
                        newReviewEditText.setText(userReview ?: "")

                        editDialog.show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@WatchedListActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    }
