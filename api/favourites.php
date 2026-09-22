<?php
// Saved (favourite) events.

// Checks that an event id looks right, or stops with an error
function checkEventId($eventId)
{
    if (!preg_match('/^[A-Za-z0-9_-]+$/', $eventId)) {
        sendError('Please send a valid event id.', 400);
    }
}

// GET /api/favourites
function listFavourites($config, $user)
{
    $path  = 'users/' . $user['uid'] . '/favourites';
    $saved = getDatabase($config)->getReference($path)->getValue();

    $list = [];
    if (is_array($saved)) {
        foreach ($saved as $event) {
            $list[] = $event;
        }
        // Newest first
        usort($list, function ($a, $b) {
            return ($b['savedAt'] ?? 0) <=> ($a['savedAt'] ?? 0);
        });
    }

    sendJson(['favourites' => $list]);
}

// POST /api/favourites   with body {"eventId": "..."}
function addFavourite($config, $user)
{
    $body    = readJsonBody();
    $eventId = trim($body['eventId'] ?? '');
    checkEventId($eventId);

    $ref = getDatabase($config)->getReference('users/' . $user['uid'] . '/favourites/' . $eventId);

    // Already saved?
    if ($ref->getSnapshot()->exists()) {
        sendError('This event is already saved.', 409);
    }

    // Ask Ticketmaster for the event, so we can save a copy of its details
    [$status, $data] = callTicketmaster("events/$eventId.json", [], $config['ticketmaster_key']);
    if ($status == 404) {
        sendError('Event not found.', 404);
    }
    if ($status != 200 || $data === null) {
        sendError('Could not get the event from Ticketmaster. Please try again.', 502);
    }

    $event = simplifyEvent($data, 200);
    $event['savedAt'] = time();

    $ref->set($event);
    sendJson($event, 201);
}

// DELETE /api/favourites/{id}
function removeFavourite($config, $user, $eventId)
{
    checkEventId($eventId);

    $ref = getDatabase($config)->getReference('users/' . $user['uid'] . '/favourites/' . $eventId);

    if (!$ref->getSnapshot()->exists()) {
        sendError('That event is not in your saved events.', 404);
    }

    $ref->remove();
    sendJson(['message' => 'Removed from saved events.']);
}