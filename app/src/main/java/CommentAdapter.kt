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
        val comment = comments[position]
        contentTextView.text = comment.content

        usernameTextView.text = comment.username
        contentTextView.text = comment.content
        val uid = comment.uid
        if (uid.isNotEmpty()) {
        db.collection("users").document(comment.uid)
            .get()
            .addOnSuccessListener { document ->
                val avatarUrl = document.getString("avatar") ?: ""
                if (avatarUrl.isNotEmpty()) {
                    Picasso.get().load(avatarUrl).into(avatarImageView)
                } else {
                    avatarImageView.setImageResource(R.drawable.default_avatar)
                }
            }} else {
            // fallback if uid empty
            avatarImageView.setImageResource(R.drawable.default_avatar)
        }


    }

    override fun getItemCount(): Int{
        return comments.size
    }
}
