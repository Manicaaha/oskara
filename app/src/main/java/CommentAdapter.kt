import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.oskarchatter.R

class CommentAdapter (private var comments: MutableList<Comment>) : RecyclerView.Adapter<CommentAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentAdapter.MyViewHolder, position: Int) {

    }
    override fun getItemCount(): Int {
        return comments.size
    }

}