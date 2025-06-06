
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.oskarchatter.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso

class MyAdapter (var posts: MutableList<Post>) :RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    private var auth = Firebase.auth
    val db = Firebase.firestore
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val post = posts[position]
        val username: TextView = holder.itemView.findViewById(R.id.username_text)
        val description: TextView = holder.itemView.findViewById(R.id.desc_post)
        val avatarImageView: ImageView = holder.itemView.findViewById(R.id.avatarImageView)
        val imageView1: ImageView = holder.itemView.findViewById(R.id.imageView11)
        val imageView2: ImageView = holder.itemView.findViewById(R.id.imageView12)

        val images = post.imageUrls ?: emptyList()

        if (images.isNotEmpty()) {
            Picasso.get().load(images[0]).placeholder(R.drawable.baseline_add_photo_alternate_24).into(imageView1)
        } else {
            imageView1.setImageResource(R.drawable.baseline_add_photo_alternate_24)
        }

// Load second image if exists
        if (images.size > 1) {
            Picasso.get().load(images[1]).placeholder(R.drawable.baseline_add_photo_alternate_24).into(imageView2)
        } else {
            imageView2.setImageResource(R.drawable.baseline_add_photo_alternate_24)
        }

        username.text = post.username
        description.text = post.content

        if (!post.avatarUrl.isNullOrEmpty()) {
            Picasso.get().load(post.avatarUrl)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(avatarImageView)
        } else {
            Picasso.get().load(R.drawable.default_avatar).into(avatarImageView)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}