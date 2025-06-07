package com.example.oskarchatter

import MyAdapter
import android.content.Intent

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LikedActivity : AppCompatActivity() {

    private lateinit var likedRecyclerView: RecyclerView
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liked)

        likedRecyclerView = findViewById(R.id.LikedRecyclerView)
        likedRecyclerView.layoutManager = LinearLayoutManager(this)

        loadLikedPosts()

        val backArrow: ImageView = findViewById(R.id.backarrow2)
        val adding: ImageView = findViewById(R.id.add_liked)
        val posts: ImageView = findViewById(R.id.posts_liked)
        val user: ImageView = findViewById(R.id.user_liked)


        backArrow.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        adding.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
            finish()
        }
        posts.setOnClickListener {
            val intent = Intent(this, PostsActivity::class.java)
            startActivity(intent)
            finish()
        }
        user.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun loadLikedPosts() {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser ?: return

        val likedPosts = mutableListOf<Post>()

        db.collection("likes")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { likeDocs ->
                val postIds = likeDocs.mapNotNull { it.getString("postId") }

                if (postIds.isEmpty()) {
                    adapter = MyAdapter(likedPosts)
                    likedRecyclerView.adapter = adapter
                    return@addOnSuccessListener
                }

                db.collection("posts")
                    .whereIn(FieldPath.documentId(), postIds)
                    .get()
                    .addOnSuccessListener { postDocs ->
                        for (doc in postDocs) {
                            val data = doc.toObject(Post::class.java)
                            val postWithId = Post(
                                id = doc.id,
                                userId = data.userId,
                                username = data.username,
                                avatarUrl = data.avatarUrl,
                                content = data.content,
                                imageUrls = data.imageUrls,
                                likeCount = data.likeCount
                            )
                            likedPosts.add(postWithId)
                        }

                        adapter = MyAdapter(likedPosts)
                        likedRecyclerView.adapter = adapter
                    }
            }
    }
}
