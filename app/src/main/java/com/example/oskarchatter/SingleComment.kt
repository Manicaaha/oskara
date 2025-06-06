package com.example.oskarchatter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SingleComment : AppCompatActivity() {

    private lateinit var commentInput: EditText
    private lateinit var sendButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_post)

        commentInput = findViewById(R.id.cmnt_Edittext)
        sendButton = findViewById(R.id.addCmnt)
        recyclerView = findViewById(R.id.CmntRecyclerView)

        commentAdapter = CommentAdapter(comments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = commentAdapter

        val postId = intent.getStringExtra("postId")
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (postId == null || currentUser == null) {
            Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        sendButton.setOnClickListener {
            val content = commentInput.text.toString().trim()
            if (content.isEmpty()) return@setOnClickListener

            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { userDoc ->
                    val username = userDoc.getString("username") ?: "Unknown"
                    val avatarUrl = userDoc.getString("avatarUrl") ?: ""

                    val comment = hashMapOf(
                        "content" to content,
                        "userId" to currentUser.uid,
                        "username" to username,
                        "avatarUrl" to avatarUrl,
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("posts").document(postId)
                        .collection("comments")
                        .add(comment)
                        .addOnSuccessListener {
                            commentInput.text.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show()
                        }
                }
        }

        db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                comments.clear()
                for (doc in snapshot.documents) {
                    val content = doc.getString("content") ?: ""
                    val username = doc.getString("username") ?: "Unknown"
                    val avatarUrl = doc.getString("avatarUrl") ?: ""
                    comments.add(Comment(content, username, avatarUrl))
                }
                commentAdapter.notifyDataSetChanged()
            }
    }
}
