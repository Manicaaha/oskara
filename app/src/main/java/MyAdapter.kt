import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.oskarchatter.R
import com.example.oskarchatter.SingleComment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class MyAdapter(private var posts: MutableList<Post>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val post = posts[position]

        val usernameText: TextView = holder.itemView.findViewById(R.id.username_text)
        val descriptionText: TextView = holder.itemView.findViewById(R.id.desc_post)
        val avatarImageView: ImageView = holder.itemView.findViewById(R.id.avatarImageView)
        val imageView1: ImageView = holder.itemView.findViewById(R.id.imageView11)
        val imageView2: ImageView = holder.itemView.findViewById(R.id.imageView12)
        val likeImage: ImageView = holder.itemView.findViewById(R.id.likeImageView)
        val commentCountTextView: TextView = holder.itemView.findViewById(R.id.commentCountTextView)
        val likeCountText: TextView = holder.itemView.findViewById(R.id.textView11)
        val commentImageView: ImageView = holder.itemView.findViewById(R.id.CommentImageView)
        val removeImageView: ImageView = holder.itemView.findViewById(R.id.removeImg)

        usernameText.text = post.username
        descriptionText.text = post.content
        val currentUser = auth.currentUser

        if (currentUser != null && post.userId == currentUser.uid) {
            removeImageView.visibility = View.VISIBLE
        } else {
            removeImageView.visibility = View.GONE
        }

        db.collection("comments")
            .whereEqualTo("postID", post.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.size()
                commentCountTextView.text = count.toString()
            }
            .addOnFailureListener {
                commentCountTextView.text = "0"
            }

        removeImageView.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            post.id?.let { postId ->
                db.collection("posts").document(postId)
                    .delete()
                    .addOnSuccessListener {
                        posts.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, posts.size)
                        Toast.makeText(holder.itemView.context, "Post deleted", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            holder.itemView.context,
                            "Failed to delete post: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        commentImageView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, SingleComment::class.java)
            intent.putExtra("postId", post.id)
            context.startActivity(intent)
        }

        if (!post.avatarUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(post.avatarUrl)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(avatarImageView)
        } else {
            avatarImageView.setImageResource(R.drawable.default_avatar)
        }

        val images = post.imageUrls ?: emptyList()
        if (images.isNotEmpty()) {
            imageView1.visibility = View.VISIBLE
            Picasso.get().load(images[0]).placeholder(R.drawable.baseline_add_photo_alternate_24)
                .into(imageView1)
        } else {
            imageView1.visibility = View.GONE
        }

        if (images.size > 1) {
            imageView2.visibility = View.VISIBLE
            Picasso.get().load(images[1]).placeholder(R.drawable.baseline_add_photo_alternate_24)
                .into(imageView2)
        } else {
            imageView2.visibility = View.GONE
        }

        likeCountText.text = post.likeCount.toString()


        if (currentUser == null || post.id == null) {
            likeImage.setImageResource(R.drawable.outline_heart_plus_24)
            likeImage.isEnabled = false
            return
        }

        val userId = currentUser.uid
        val postId = post.id
        val likeDocId = "${postId}_$userId"
        val likeRef = db.collection("likes").document(likeDocId)
        val postRef = db.collection("posts").document(postId)

        likeRef.get().addOnSuccessListener { snapshot ->
            val isLiked = snapshot.exists()
            likeImage.setImageResource(
                if (isLiked) R.drawable.ic_heart_filled else R.drawable.outline_heart_plus_24
            )
            postRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val latestLikeCount = documentSnapshot.getLong("likeCount") ?: 0L
                    likeCountText.text = latestLikeCount.toString()
                    post.likeCount = latestLikeCount.toInt()
                } else {
                    likeCountText.text = "0"
                    post.likeCount = 0
                }
            }.addOnFailureListener {
                likeCountText.text = post.likeCount.toString()
            }

            likeImage.setOnClickListener {
                likeRef.get().addOnSuccessListener { snapshot ->
                    val isLiked = snapshot.exists()
                    if (isLiked) {
                        likeRef.delete()
                        postRef.update("likeCount", post.likeCount - 1)
                        post.likeCount--
                        notifyItemChanged(position)
                        likeCountText.text = post.likeCount.toString()
                    } else {
                        likeRef.set(mapOf("userId" to userId, "postId" to postId))
                        postRef.update("likeCount", post.likeCount + 1)
                        post.likeCount++
                        notifyItemChanged(position)
                        likeCountText.text = post.likeCount.toString()
                    }
                }
            }

            commentImageView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, SingleComment::class.java)
                intent.putExtra("postId", post.id)
                intent.putExtra("postUsername", post.username)
                intent.putExtra("postContent", post.content)
                intent.putExtra("postAvatarUrl", post.avatarUrl)
                intent.putExtra("postImageUrl1", if (images.isNotEmpty()) images[0] else "")
                intent.putExtra("postImageUrl2", if (images.size > 1) images[1] else "")
                context.startActivity(intent)
            }


        }
    }
        override fun getItemCount(): Int {
            return posts.size
        }
    }
