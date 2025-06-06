package com.example.oskarchatter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameTextView: TextView = view.findViewById(R.id.comment_username)
        val contentTextView: TextView = view.findViewById(R.id.comment_content)
        val avatarImageView: ImageView = view.findViewById(R.id.comment_avatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.contentTextView.text = comment.content
        holder.usernameTextView.text = comment.username
        if (comment.avatarUrl.isNotEmpty()) {
            Picasso.get().load(comment.avatarUrl)
                .placeholder(R.drawable.default_avatar)
                .into(holder.avatarImageView)
        } else {
            holder.avatarImageView.setImageResource(R.drawable.default_avatar)
        }
    }

    override fun getItemCount() = comments.size
}
