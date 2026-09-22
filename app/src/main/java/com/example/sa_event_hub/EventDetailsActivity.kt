package com.example.sa_event_hub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.sa_event_hub.databinding.ActivityEventDetailsBinding

// Shows everything about one event, with a Save button and a Get Tickets button.
class EventDetailsActivity : AppCompatActivity() {

    // Other screens put the event id here before opening this screen
    companion object {
        const val EXTRA_EVENT_ID = "event_id"
    }

    private lateinit var binding: ActivityEventDetailsBinding
    private lateinit var viewModel: EventDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID)
        if (eventId.isNullOrBlank()) {
            finish() // something went wrong, so just close this screen
            return
        }

        viewModel = ViewModelProvider(this)[EventDetailsViewModel::class.java]

        binding.tvBack.setOnClickListener { finish() }



        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (loading) View.GONE else View.VISIBLE
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.event.observe(this) { event ->
            if (event != null) showEvent(event)
        }

        viewModel.isSaved.observe(this) { saved ->
            binding.btnSave.text = if (saved) getString(R.string.saved) else getString(R.string.save_event)
        }

        binding.btnSave.setOnClickListener {
            viewModel.toggleSave(eventId)
        }

        viewModel.loadEvent(eventId)
    }

    // Fills the screen with one event's details
    private fun showEvent(event: Event) {
        binding.tvCategory.text = (event.category ?: "").uppercase()
        binding.tvTitle.text = event.title
        binding.tvDateTime.text = EventFormat.dateAndTime(event.date, event.time)

        binding.tvVenue.text = listOfNotNull(event.venue, event.city)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        binding.tvDescription.text =
            if (event.description.isNullOrBlank()) getString(R.string.no_description) else event.description

        Glide.with(binding.ivImage)
            .load(event.imageUrl)
            .centerCrop()
            .into(binding.ivImage)

        // Opens Ticketmaster's own page. We never handle payments ourselves.
        binding.btnGetTickets.setOnClickListener {
            val url = event.ticketUrl
            if (url.isNullOrBlank()) {
                Toast.makeText(this, getString(R.string.no_ticket_link), Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }
}