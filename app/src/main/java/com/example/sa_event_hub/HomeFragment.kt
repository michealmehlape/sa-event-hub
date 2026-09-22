package com.example.sa_event_hub

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sa_event_hub.databinding.FragmentHomeBinding

// The Home tab: a list of upcoming events.
class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)

        // We share the ViewModel with the whole activity, so the events
        // are still there when the user switches tabs and comes back.
        val viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        // Set up the list
        val adapter = EventAdapter { event ->
            val intent = Intent(requireContext(), EventDetailsActivity::class.java)
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.id)
            startActivity(intent)
        }
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.adapter = adapter

        // WATCH the ViewModel and update the screen when something changes

        viewModel.events.observe(viewLifecycleOwner) { list ->
            adapter.setEvents(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message == null) {
                binding.errorLayout.visibility = View.GONE
            } else {
                binding.tvError.text = message
                binding.errorLayout.visibility = View.VISIBLE
            }
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadEvents()
        }

        // Load the events the first time only
        if (viewModel.events.value.isNullOrEmpty()) {
            viewModel.loadEvents()
        }
    }
}