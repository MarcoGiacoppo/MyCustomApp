package com.example.mycustomapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycustomapp.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

private lateinit var auth: FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val registerButton = findViewById<Button>(R.id.signupBtn)
        registerButton.setOnClickListener {
            // Get references to EditTexts for user input
            val usernameEditText = findViewById<EditText>(R.id.registerUser)
            val passwordEditText = findViewById<EditText>(R.id.registerPass)
            val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPass)

            // Get user input values
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Perform basic validation
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register the user with Firebase Authentication
            auth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null){
                            val userId = user.uid
                            val displayName = user.displayName
                            val email = user.email

                            // Create a reference to the "Users" node in the database
                            val database = FirebaseDatabase.getInstance()
                            val userRef = database.getReference("Users")

                            // Create a user profile entry
                            val userProfile = UserProfile(userId, displayName, email)

                            // Set the user profile in the database
                            userRef.child(userId).setValue(userProfile)
                        }

                        Toast.makeText(this, "Account successfully registered!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
