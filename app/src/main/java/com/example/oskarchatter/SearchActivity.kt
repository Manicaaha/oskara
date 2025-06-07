package com.example.oskarchatter

import MyAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private val posts = mutableListOf<Post>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backarrow: ImageView = findViewById(R.id.backarrow3)
        val adding: ImageView = findViewById(R.id.add_search)
        val postsImageView: ImageView = findViewById(R.id.posts_search)
        val user: ImageView = findViewById(R.id.user_search)



        backarrow.setOnClickListener {
            val intent  = Intent(this, PostsActivity::class.java)
            startActivity(intent)
            finish()
        }
        adding.setOnClickListener {
            val intent  = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
            finish()
        }
        postsImageView.setOnClickListener {
            val intent  = Intent(this, PostsActivity::class.java)
            startActivity(intent)
            finish()
        }
        user.setOnClickListener {
            val intent  = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        searchInput = findViewById(R.id.editTextText2)
        recyclerView = findViewById(R.id.searchRecyclerView)
        adapter = MyAdapter(posts)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        searchInput.addTextChangedListener {
            val query = it.toString().trim()
            if (query.isNotEmpty()) {
                searchPosts(query.lowercase())
            } else {
                posts.clear()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun searchPosts(query: String) {
        db.collection("posts")
            .get()
            .addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    val content = document.getString("content").orEmpty().lowercase()
                    if (content.contains(query)) {
                        val post = Post(
                            id = document.id,
                            username = document.getString("username").orEmpty(),
                            content = document.getString("content").orEmpty(),
                            avatarUrl = document.getString("avatarUrl"),
                            imageUrls = document.get("imageUrls") as? List<String> ?: emptyList(),
                            userId = document.getString("userId").orEmpty(),
                            likeCount = document.getLong("likeCount")?.toInt() ?: 0
                        )
                        posts.add(post)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error searching posts: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

