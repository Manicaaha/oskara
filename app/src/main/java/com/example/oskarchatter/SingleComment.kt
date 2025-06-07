package com.example.oskarchatter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class SingleComment : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_post)
        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val userTextView: TextView = findViewById(R.id.textView10)
        val avatarImageView: ImageView = findViewById(R.id.imageView5)
        val contentTextView: TextView = findViewById(R.id.textView13)
        val postImageView1: ImageView = findViewById(R.id.img1)
        val postImageView2: ImageView = findViewById(R.id.img2)
        val backarrow: ImageView = findViewById(R.id.backarrow)

        val postId = intent.getStringExtra("postId")
        val postUsername = intent.getStringExtra("postUsername") ?: "Unknown"
        val postContent = intent.getStringExtra("postContent") ?: ""
        val avatarUrl = intent.getStringExtra("postAvatarUrl")
        val postImageUrl1 = intent.getStringExtra("postImageUrl1")
        val postImageUrl2 = intent.getStringExtra("postImageUrl2")

        if (!avatarUrl.isNullOrEmpty()) {
            Picasso.get().load(avatarUrl).into(avatarImageView)
        } else {
            avatarImageView.setImageResource(R.drawable.default_avatar)
        }

        backarrow.setOnClickListener {
            val intent = Intent(this, PostsActivity::class.java)
            startActivity(intent)
            finish()
        }

        userTextView.text = postUsername
        contentTextView.text = postContent

        if (!postImageUrl1.isNullOrEmpty()) {
            postImageView1.visibility = View.VISIBLE
            Picasso.get()
                .load(postImageUrl1).into(postImageView1)
        } else {
            postImageView1.visibility = View.GONE
        }

        if (!postImageUrl2.isNullOrEmpty()) {
            postImageView2.visibility = View.VISIBLE
            Picasso.get()
                .load(postImageUrl2).into(postImageView2)
        } else {
            postImageView2.visibility = View.GONE
        }

        val commentInput: EditText = findViewById(R.id.cmnt_Edittext)
        val sendButton: Button = findViewById(R.id.addCmnt)
        val recyclerView: RecyclerView = findViewById(R.id.CmntRecyclerView)


        commentAdapter = CommentAdapter(comments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = commentAdapter



        val currentUser = FirebaseAuth.getInstance().currentUser

        if (postId == null || currentUser == null) {
            Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        sendButton.setOnClickListener {
            val content = commentInput.text.toString()
            if (content.isEmpty()) return@setOnClickListener

            val email = currentUser.email ?: ""
            val usernameFromEmail = email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }

            val comment = hashMapOf(
                "content" to content,
                "uid" to currentUser.uid,
                "username" to usernameFromEmail,
                "postID" to postId,
                "avatarUrl" to ""
            )

            db.collection("comments")
                .add(comment)
                .addOnSuccessListener {
                    commentInput.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show()
                }
        }

        loadComments(postId)

    }
    private fun loadComments(postId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("comments")
            .whereEqualTo("postID", postId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                comments.clear()
                for (doc in snapshots!!) {
                    val comment = Comment(
                        uid = doc.getString("uid") ?: "",
                        content = doc.getString("content") ?: "",
                        username = doc.getString("username") ?: "Unknown",
                        postID = doc.getString("postID") ?: "",
                        avatarUrl = doc.getString("avatarUrl") ?: ""
                    )
                    comments.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
    }

}
