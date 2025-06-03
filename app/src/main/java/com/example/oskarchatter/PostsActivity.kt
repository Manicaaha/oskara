package com.example.oskarchatter

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class PostsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        val home: ImageView = findViewById(R.id.posts_home)
        val user: ImageView = findViewById(R.id.posts_user)
        val add:ImageView = findViewById(R.id.posts_add)
        val avatar2: ImageView = findViewById(R.id.userProfileImage3)
        val add_pic: ImageView = findViewById(R.id.posts_pic)
        val add_cam: ImageView = findViewById(R.id.posts_cam)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()


        if (currentUser != null) {
            val uid = currentUser.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val avatarUrl = document.getString("avatar")
                        if (!avatarUrl.isNullOrEmpty()) {
                            Picasso.get().load(avatarUrl).into(user)
                            Picasso.get().load(avatarUrl).into(avatar2)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }

        home.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        user.setOnClickListener{
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        add.setOnClickListener{
            startActivity(Intent(this, AddPostActivity::class.java))
            finish()
        }
        add_pic.setOnClickListener{
            startActivity(Intent(this, AddPostActivity::class.java))
            finish()
        }
        add_cam.setOnClickListener{
            startActivity(Intent(this, AddPostActivity::class.java))
            finish()
        }
    }
}