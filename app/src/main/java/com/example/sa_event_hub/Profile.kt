package com.example.sa_event_hub

// What GET /api/profile sends back (we only read the fields we need)
data class ProfileResponse(
    val name: String?,
    val email: String?,
    val photoUrl: String?

)
// What we send to PUT /api/profile (fields left as null are not sent)
data class ProfileUpdate(
    val fcmToken: String? = null,
    val notificationsEnabled: Boolean? = null
)

// What POST /api/profile/photo sends back
data class PhotoResponse(val photoUrl: String)
