package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sa_event_hub.databinding.FragmentCommunityBinding

// The Community tab. Shows posts from all users and allows a new post to be made.
class CommunityFragment : Fragment(R.layout.fragment_community) {

    private lateinit var binding: FragmentCommunityBinding
    private lateinit var viewModel: CommunityViewModel
    private lateinit var adapter: PostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCommunityBinding.bind(view)
        viewModel = ViewModelProvider(this)[CommunityViewModel::class.java]

        adapter = PostAdapter { post ->
            val intent = Intent(requireContext(), PostDetailsActivity::class.java)
            intent.putExtra(PostDetailsActivity.EXTRA_POST_ID, post.id)
            intent.putExtra(PostDetailsActivity.EXTRA_POST_AUTHOR, post.authorName)
            intent.putExtra(PostDetailsActivity.EXTRA_POST_BODY, post.body)
            startActivity(intent)
        }
        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter

        binding.btnPost.setOnClickListener {
            val text = binding.etNewPost.text.toString()
            viewModel.createPost(text)
        }

        viewModel.posts.observe(viewLifecycleOwner) { list -> adapter.setPosts(list) }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            binding.tvEmpty.text = message
            binding.tvEmpty.visibility = if (message != null) View.VISIBLE else View.GONE
        }

        viewModel.postCreated.observe(viewLifecycleOwner) { created ->
            if (created) {
                binding.etNewPost.text?.clear()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPosts()
    }
}