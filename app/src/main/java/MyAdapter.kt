import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.books_talk.R
import com.example.books_talk.SinglebookActivity
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
        val username: TextView = holder.itemView.findViewById(R.id.username_text)
        val description: TextView = holder.itemView.findViewById(R.id.desc_post)

        username.text = posts[position].username
        description.text = posts[position].content
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}