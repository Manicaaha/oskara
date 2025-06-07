package com.example.oskarchatter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val usernameTextView: TextView = holder.itemView.findViewById(R.id.comment_username)
        val contentTextView: TextView = holder.itemView.findViewById(R.id.comment_content)
        val avatarImageView: ImageView = holder.itemView.findViewById(R.id.comment_avatar)
        val user = FirebaseAuth.getInstance().currentUser
        val comment = comments[position]
        contentTextView.text = comment.content
        usernameTextView.text = comment.username

        user?.email?.let {
            val cleanedEmail = it.split(".").first()
            val usernamePart = cleanedEmail.substringBefore("@")
            val formattedUsername = usernamePart.replaceFirstChar { it.uppercaseChar() }
            usernameTextView.text = formattedUsername
        }

        val docRef = db.collection("users").document(auth.currentUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val avatarUrl = document.getString("avatar")
                    Log.d("COmmentAdapter", "Fetched avatar URL: $avatarUrl")
                    if (!avatarUrl.isNullOrEmpty()) {
                        Picasso.get().load(avatarUrl)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(avatarImageView)
                    } else {
                        Log.d("COmmentAdapter", "Avatar URL is null or empty")
                        Picasso.get().load(R.drawable.default_avatar).into(avatarImageView)
                    }
                } else {
                    Log.d("COmmentAdapter", "User document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("COmmentAdapter", "get failed with ", exception)
                Picasso.get().load(R.drawable.default_avatar).into(avatarImageView)
            }

    }

    override fun getItemCount() = comments.size
}
