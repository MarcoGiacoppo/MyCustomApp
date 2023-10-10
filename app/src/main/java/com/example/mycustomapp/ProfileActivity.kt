package com.example.mycustomapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var avatarSelectionGroup: RadioGroup
    private lateinit var saveProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profileImageView)
        avatarSelectionGroup = findViewById(R.id.avatarSelectionGroup)
        saveProfileButton = findViewById(R.id.saveProfile)

        // Set a listener for avatar selection changes
        avatarSelectionGroup.setOnCheckedChangeListener { _, checkedId ->
            // Handle avatar selection here
            val selectedAvatar: Int = when (checkedId) {
                R.id.avatarOption1 -> R.drawable.avatar1
                R.id.avatarOption2 -> R.drawable.avatar2
                R.id.avatarOption3 -> R.drawable.avatar3
                R.id.avatarOption4 -> R.drawable.avatar4
                R.id.avatarOption5 -> R.drawable.avatar5
                R.id.avatarOption6 -> R.drawable.avatar6
                R.id.avatarOption7 -> R.drawable.avatar7
                R.id.avatarOption8 -> R.drawable.avatar8

                else -> R.drawable.ic_round_person_24 // Default avatar
            }

            // Set the selected avatar as the profile picture
            profileImageView.setImageResource(selectedAvatar)
        }

        // Set a click listener for the Edit Profile button
        saveProfileButton.setOnClickListener {
            // Call the function to save the avatar selection
            saveAvatarSelection()
            // Handle editing of user profile here
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val cancelButton = findViewById<ImageView>(R.id.cancel)
        cancelButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveAvatarSelection() {
        // Get the currently logged-in user
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid

            // Get the selected avatar identifier from the RadioGroup
            val selectedAvatarId = when (avatarSelectionGroup.checkedRadioButtonId) {
                R.id.avatarOption1 -> "avatar1"
                R.id.avatarOption2 -> "avatar2"
                R.id.avatarOption3 -> "avatar3"
                R.id.avatarOption4 -> "avatar4"
                R.id.avatarOption5 -> "avatar5"
                R.id.avatarOption6 -> "avatar6"
                R.id.avatarOption7 -> "avatar7"
                R.id.avatarOption8 -> "avatar8"
                // Add more cases for additional avatar options
                else -> "default_avatar" // Default avatar
            }

            // Update the user's "avatar" field in the database
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("Users")

            usersRef.child(userId).child("avatar").setValue(selectedAvatarId)

            // Inform the user that the avatar selection has been saved
            Toast.makeText(this, "Avatar selection saved.", Toast.LENGTH_SHORT).show()
        }
    }
}

