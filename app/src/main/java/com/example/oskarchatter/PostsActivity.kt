package com.example.oskarchatter

import MyAdapter
import Post
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class PostsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        val posts = mutableListOf<Post>()
        val home: ImageView = findViewById(R.id.posts_home)
        val user: ImageView = findViewById(R.id.posts_user)
        val add:ImageView = findViewById(R.id.posts_add)
        val gotoadd: TextView = findViewById(R.id.textView8)
        val username: TextView = findViewById(R.id.username_postActivity)
        val avatar2: ImageView = findViewById(R.id.userProfileImage3)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        val myAdapter = MyAdapter(posts)
        val myRecyclerView: RecyclerView = findViewById(R.id.RecyclerView)
        myRecyclerView.adapter = myAdapter
        myRecyclerView.layoutManager = LinearLayoutManager(this)

        currentUser?.email?.let {
            val cleanedEmail = it.split(".").first()
            val usernamePart = cleanedEmail.substringBefore("@")
            val formattedUsername = usernamePart.replaceFirstChar { c -> c.uppercaseChar() }
            username.text = formattedUsername
        }

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
        gotoadd.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
            finish()
        }
        db.collection("posts").get()
            .addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    val postId = document.id
                    val username = document.getString("username") ?: "Unknown"
                    val content = document.getString("content") ?: ""
                    val avatarUrl = document.getString("avatarUrl")
                    val imageUrls = document.get("imageUrls") as? List<String> ?: emptyList()

                    posts.add(Post(postId, username, content, avatarUrl, imageUrls))

                }
                myAdapter.notifyDataSetChanged()
            }

    }
}