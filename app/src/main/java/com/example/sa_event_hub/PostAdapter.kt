package com.example.sa_event_hub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sa_event_hub.databinding.ItemPostBinding
import java.text.DateFormat
import java.util.Date

// Displays a list of community posts. Tapping a post opens its comments.
class PostAdapter(
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var posts: List<Post> = emptyList()

    fun setPosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.tvAuthor.text = post.authorName
        holder.binding.tvBody.text = post.body
        holder.binding.tvTime.text = formatTime(post.createdAt)
        holder.itemView.setOnClickListener { onPostClick(post) }
    }

    override fun getItemCount(): Int = posts.size

    // Turns a Unix timestamp (seconds) into a readable date and time.
    private fun formatTime(seconds: Long): String {
        val date = Date(seconds * 1000)
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date)
    }
}