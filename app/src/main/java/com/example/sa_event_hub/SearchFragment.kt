package com.example.sa_event_hub

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sa_event_hub.databinding.FragmentSearchBinding

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: EventAdapter

    // What we show the user for each city, and what we send to the API.
    // "Any city" sends null, meaning "no city filter".
    private val cityLabels = listOf("Any city", "Johannesburg", "Cape Town", "Pretoria", "Durban")
    private val cityValues = listOf(null, "Johannesburg", "Cape Town", "Pretoria", "Durban")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        // We use our own ViewModel here (not shared), because Search keeps its own filters separate from Home's list.
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        setUpList()
        setUpSearchBox()
        setUpCategoryChips()
        setUpCityDropdown()
        watchViewModel()

        // Show results immediately, with no filters, when the tab first opens
        if (viewModel.results.value.isNullOrEmpty()) {
            viewModel.search()
        }
    }

    private fun setUpList() {
        adapter = EventAdapter { event ->
            val intent = Intent(requireContext(), EventDetailsActivity::class.java)
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.id)
            startActivity(intent)
        }
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.adapter = adapter
    }

    private fun setUpSearchBox() {
        // Search only runs when the user presses the Search key on the keyboard
        binding.etSearch.setOnEditorActionListener { textView, actionId, event ->
            val pressedSearch = actionId == EditorInfo.IME_ACTION_SEARCH
            val pressedEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER
            if (pressedSearch || pressedEnter) {
                viewModel.setKeyword(textView.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun setUpCategoryChips() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            val category = when (checkedIds.firstOrNull()) {
                binding.chipMusic.id -> "Music"
                binding.chipSports.id -> "Sports"
                binding.chipArts.id -> "Arts & Culture"
                binding.chipComedy.id -> "Comedy"
                else -> null   // "All" chip, or nothing chosen
            }
            viewModel.setCategory(category)
        }
    }

    private fun setUpCityDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, cityLabels)
        binding.spinnerCity.setAdapter(adapter)
        binding.spinnerCity.setText(cityLabels[0], false)   // start on "Any city"

        binding.spinnerCity.setOnItemClickListener { _, _, position, _ ->
            viewModel.setCity(cityValues[position])
        }
    }

    private fun watchViewModel() {
        viewModel.results.observe(viewLifecycleOwner) { list ->
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
}