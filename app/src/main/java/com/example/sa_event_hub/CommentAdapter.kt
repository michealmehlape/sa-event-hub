package com.example.sa_event_hub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sa_event_hub.databinding.ItemCommentBinding

// Displays the list of comments underneath a post.
class CommentAdapter : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private var comments: List<Comment> = emptyList()

    fun setComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }

    class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.binding.tvAuthor.text = comment.authorName
        holder.binding.tvBody.text = comment.body
    }

    override fun getItemCount(): Int = comments.size
}