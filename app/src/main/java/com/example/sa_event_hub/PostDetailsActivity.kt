package com.example.sa_event_hub

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sa_event_hub.databinding.ActivityPostDetailsBinding

// Shows one post together with its comments, and allows a new comment to be added.
class PostDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "post_id"
        const val EXTRA_POST_AUTHOR = "post_author"
        const val EXTRA_POST_BODY = "post_body"
    }

    private lateinit var binding: ActivityPostDetailsBinding
    private lateinit var viewModel: PostDetailsViewModel
    private lateinit var adapter: CommentAdapter
    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra(EXTRA_POST_ID) ?: run { finish(); return }
        binding.tvAuthor.text = intent.getStringExtra(EXTRA_POST_AUTHOR) ?: ""
        binding.tvBody.text = intent.getStringExtra(EXTRA_POST_BODY) ?: ""

        viewModel = ViewModelProvider(this)[PostDetailsViewModel::class.java]

        adapter = CommentAdapter()
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = adapter

        binding.tvBack.setOnClickListener { finish() }

        binding.btnComment.setOnClickListener {
            val text = binding.etComment.text.toString()
            viewModel.addComment(postId, text)
            binding.etComment.text?.clear()
        }

        viewModel.comments.observe(this) { list -> adapter.setComments(list) }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { message ->
            binding.tvEmpty.text = message
            binding.tvEmpty.visibility = if (message != null) View.VISIBLE else View.GONE
        }

        viewModel.loadComments(postId)
    }
}