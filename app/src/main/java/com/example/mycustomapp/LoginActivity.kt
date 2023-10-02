package com.example.mycustomapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.graphics.Paint

private lateinit var auth: FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Set a click listener on the Login button
        val login = findViewById<Button>(R.id.loginBtn)
        login.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.editTextText)
            val passwordEditText = findViewById<EditText>(R.id.editTextPassword)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                // Display a toast message if email or password is empty
                Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show()
            } else {
                // Attempt to sign in with Firebase Authentication
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // If successful, navigate to MainActivity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            //Login failed, handle error
                            Toast.makeText(this, "Wrong Username or Password.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }

        val forgetPasswordTxt = findViewById<TextView>(R.id.forgetTxt)
        forgetPasswordTxt.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        // Set a click listener for the "Forgot Password?" text
        forgetPasswordTxt.setOnClickListener{
            val emailEditText = findViewById<EditText>(R.id.editTextText)
            val email = emailEditText.text.toString()

            if (email.isEmpty()){
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
            } else {
                // Send a password reset email to user's email address
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful){
                            Toast.makeText(this, "Password reset email sent. Check your email inbox.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


        val registerNowText = findViewById<TextView>(R.id.registerNowTxt)
        registerNowText.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        registerNowText.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}