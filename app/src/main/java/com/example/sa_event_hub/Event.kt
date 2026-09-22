package com.example.sa_event_hub

// One event. The names match the JSON that your API sends.
data class Event(
    val id: String,
    val title: String,
    val description: String?,
    val date: String?,
    val time: String?,
    val venue: String?,
    val city: String?,
    val category: String?,
    val imageUrl: String?,
    val ticketUrl: String?
)

// lists all the events
data class EventListResponse(
    val events: List<Event>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Int
)

// What we send when saving an event
data class FavouriteRequest(val eventId: String)

// The answer to "give me my saved events"
data class FavouriteListResponse(val favourites: List<Event>)

// A simple answer with just a message, like {"message": "Removed."}
data class MessageResponse(val message: String)