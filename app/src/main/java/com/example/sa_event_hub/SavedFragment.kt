package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sa_event_hub.databinding.FragmentSavedBinding

class SavedFragment : Fragment(R.layout.fragment_saved) {

    private lateinit var binding: FragmentSavedBinding
    private lateinit var viewModel: SavedViewModel
    private lateinit var adapter: EventAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSavedBinding.bind(view)
        viewModel = ViewModelProvider(this)[SavedViewModel::class.java]

        adapter = EventAdapter { event ->
            val intent = Intent(requireContext(), EventDetailsActivity::class.java)
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.id)
            startActivity(intent)
        }
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.adapter = adapter

        // WATCH the ViewModel

        viewModel.favourites.observe(viewLifecycleOwner) { list ->
            adapter.setEvents(list)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            binding.tvEmpty.text = message
            binding.tvEmpty.visibility = if (message != null) View.VISIBLE else View.GONE
        }
    }

    // Runs every time this tab is shown, so newly saved events appear straight away
    override fun onResume() {
        super.onResume()
        viewModel.loadFavourites()
    }
}