# SA Event Hub

An Android application for discovering events across South Africa, built for
OPSC6312 (Open Source Coding). Developed by CyberSquad.

## Group Members
- Michael Mehlape — ST0102939
- Advice Ngobene — ST10456753
- Murendeni Nethenzheni — ST10396326

## Features

- Registration and login (Firebase Authentication, encrypted passwords)
- Settings: change name, change password, notification toggle, event interests
- Event discovery, search and filtering (Ticketmaster Discovery API)
- Save and remove favourite events
- Community: create posts and comment on posts
- Offline saving with sync when the connection returns (Room + WorkManager) — *in progress*
- Push notifications (Firebase Cloud Messaging) — *in progress*
- Multi-language support (English, isiZulu, Afrikaans) — *in progress*

## Architecture

- **Android app:** Kotlin, XML layouts, MVVM (ViewModel + LiveData), Retrofit, Glide, Room.
- **Authentication:** Firebase Authentication (email/password).
- **Database:** Firebase Realtime Database, accessed only through the REST API below.
- **REST API:** PHP 8.5, hosted on Afrihost shared hosting at
  `https://saeventhub.phatuditradings.co.za/`. Source in `/api`.
- **External data:** Ticketmaster Discovery API, called from the PHP API so the
  API key is never exposed inside the Android app.

### Design change from Part 1

Part 1 proposed Firebase Cloud Functions and Cloud Firestore. Cloud Functions
requires the paid Firebase Blaze plan, so the backend was rebuilt as a PHP
REST API on existing Afrihost hosting, using Firebase Realtime Database
instead of Firestore. Firebase Authentication was kept unchanged.

## API reference 

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/events` | No | List events (filters: city, category, page, size) |
| GET | `/api/events/search?q=` | No | Search events by keyword |
| GET | `/api/events/{id}` | No | Full details for one event |
| GET | `/api/favourites` | Yes | The logged-in user's saved events |
| POST | `/api/favourites` | Yes | Save an event: `{"eventId": "..."}` |
| DELETE | `/api/favourites/{id}` | Yes | Remove a saved event |
| GET | `/api/profile` | Yes | The logged-in user's profile |
| PUT | `/api/profile` | Yes | Update name, language, interests, notifications |
| GET | `/api/community/posts` | Yes | List community posts |
| POST | `/api/community/posts` | Yes | Create a post: `{"body": "..."}` |
| GET | `/api/community/posts/{id}/comments` | Yes | List comments on a post |
| POST | `/api/community/posts/{id}/comments` | Yes | Add a comment: `{"body": "..."}` |

Authenticated endpoints require a Firebase ID token in the request header:
`Authorization: Bearer <token>`.

## Testing

Unit tests cover input validation and date formatting and live under
`app/src/test/java/com/example/sa_event_hub/`. Run them from Android Studio
GitHub Actions and unit tests were created to test the app
runs them automatically on every push (see badge and workflow below).

## Demo video


* https://youtu.be/gCHEOXtgLas?si=ukdAPbnZv6eveaa6 *

