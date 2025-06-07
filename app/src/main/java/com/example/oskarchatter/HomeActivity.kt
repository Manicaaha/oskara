package com.example.oskarchatter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = Firebase.auth

        val signOutButton: TextView = findViewById(R.id.logOutTextView)
        val avatarImage: ImageView = findViewById(R.id.avatarImage)
        val goBack: ImageView = findViewById(R.id.goBackImageView)
        val posts: ImageView = findViewById(R.id.home_home)
        val add: ImageView = findViewById(R.id.home_add)
        val liked: ImageView = findViewById(R.id.liked)
        val db = Firebase.firestore

        val user = FirebaseAuth.getInstance().currentUser

        user?.email?.let {
            val cleanedEmail = it.split(".").first()
            val usernamePart = cleanedEmail.substringBefore("@")
            val formattedUsername = usernamePart.replaceFirstChar { it.uppercaseChar() }
            val usernameTextView: TextView = findViewById(R.id.usernameTextView)
            usernameTextView.text = formattedUsername
        }

        val docRef = db.collection("users").document(auth.currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val avatarUrl = document.getString("avatar")
                    Log.d("HomeActivity", "Fetched avatar URL: $avatarUrl")
                    if (!avatarUrl.isNullOrEmpty()) {
                        Picasso.get().load(avatarUrl)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(avatarImage)
                    } else {
                        Log.d("HomeActivity", "Avatar URL is null or empty")
                        Picasso.get().load(R.drawable.default_avatar).into(avatarImage)
                    }
                } else {
                    Log.d("HomeActivity", "User document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("HomeActivity", "get failed with ", exception)
                Picasso.get().load(R.drawable.default_avatar).into(avatarImage)
            }


        signOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        posts.setOnClickListener{
            startActivity(Intent(this, PostsActivity::class.java))
            finish()
        }

        add.setOnClickListener{
            startActivity(Intent(this, AddPostActivity::class.java))
            finish()
        }
        liked.setOnClickListener{
            startActivity(Intent(this, LikedActivity::class.java))
            finish()
        }

        goBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}