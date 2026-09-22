package com.example.sa_event_hub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sa_event_hub.databinding.ItemEventBinding

// Shows a list of events. The same adapter is used on Home, Search and Saved.
// onEventClick runs when the user taps an event.
class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events: List<Event> = emptyList()

    // Gives the adapter a new list and redraws
    fun setEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    // A ViewHolder holds the views of one card
    class EventViewHolder(val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root)

    // Makes a new empty card
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    // Fills a card with one event
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val views = holder.binding

        views.tvCategory.text = (event.category ?: "").uppercase()
        views.tvTitle.text = event.title
        views.tvDate.text = EventFormat.dateAndTime(event.date, event.time)

        // Show "Venue, City" (skip the empty parts)
        views.tvVenue.text = listOfNotNull(event.venue, event.city)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        // Load the picture from the internet
        Glide.with(views.ivImage)
            .load(event.imageUrl)
            .centerCrop()
            .placeholder(R.color.card)
            .error(R.color.card)
            .into(views.ivImage)

        holder.itemView.setOnClickListener { onEventClick(event) }
    }

    override fun getItemCount(): Int = events.size
}