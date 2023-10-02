    package com.example.mycustomapp

    import android.content.Intent
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.ProgressBar
    import android.widget.RatingBar
    import android.widget.Toast
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.mycustomapp.adapters.WatchedAdapter
    import com.example.mycustomapp.databinding.ActivityWatchedListBinding
    import com.example.mycustomapp.models.WatchlistItem
    import com.google.firebase.database.*

    class WatchedListActivity : AppCompatActivity(),
        WatchedAdapter.OnDeleteItemClickListener{

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

            // Initialize your RecyclerView
            recyclerView = binding.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(this)

            // Initialize Firebase and get reference to watched movies
            watchedMoviesReference = FirebaseDatabase.getInstance().getReference("Reviews")

            // Initialize an empty list for watched movies
            val reviewList: MutableList<WatchlistItem> = mutableListOf()

            // Create a default item click listener
            val itemClickListener = object : WatchedAdapter.OnItemClickListener {
                override fun onItemClick(watchlistItem: WatchlistItem) {
                }
            }

            adapter = WatchedAdapter(reviewList, itemClickListener, this)

            val backBtn = findViewById<ImageView>(R.id.back)
            backBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            // Create a ValueEventListener to fetch data from Firebase
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Clear the previous list
                    reviewList.clear()

                    for (postSnapshot in dataSnapshot.children) {
                        // Deserialize the data into a WatchlistItem object
                        val watchlistItem = postSnapshot.getValue(WatchlistItem::class.java)

                        if (watchlistItem != null) {
                            reviewList.add(watchlistItem)
                            // Hide the loading indicator once the data has been loaded
                            loadingProgressBar.visibility = View.GONE
                        }
                    }
                    // Set the adapter for your RecyclerView
                    recyclerView.adapter = adapter
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
    }