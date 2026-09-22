<?php
// Everything about events lives in this file.

// Turns one big Ticketmaster event into a small, simple one for our app.
// $maxDescription: cut the description to this many characters (null = keep all).
function simplifyEvent($e, $maxDescription = null)
{
    // Pick a picture. We prefer the 16:9 picture that is 640 pixels wide.
    $imageUrl = null;
    $images = $e['images'] ?? [];
    foreach ($images as $img) {
        if (($img['ratio'] ?? '') === '16_9' && ($img['width'] ?? 0) == 640) {
            $imageUrl = $img['url'];
            break;
        }
    }
    if ($imageUrl === null && count($images) > 0) {
        $imageUrl = $images[0]['url'];   // otherwise just use the first picture
    }

    // The venue (place) is inside "_embedded"
    $venue = $e['_embedded']['venues'][0] ?? [];

    // The category is the classification marked as "primary"
    $category = '';
    $classifications = $e['classifications'] ?? [];
    foreach ($classifications as $c) {
        if (!empty($c['primary'])) {
            $category = $c['segment']['name'] ?? '';
            break;
        }
    }
    if ($category === '' && count($classifications) > 0) {
        $category = $classifications[0]['segment']['name'] ?? '';
    }

    // The description may be missing, or very long
    $description = $e['info'] ?? '';
    if ($maxDescription !== null && mb_strlen($description) > $maxDescription) {
        $description = mb_substr($description, 0, $maxDescription) . '...';
    }

    return [
        'id'          => $e['id'] ?? '',
        'title'       => $e['name'] ?? '',
        'description' => $description,
        'date'        => $e['dates']['start']['localDate'] ?? null,
        'time'        => $e['dates']['start']['localTime'] ?? null,
        'venue'       => $venue['name'] ?? '',
        'city'        => $venue['city']['name'] ?? '',
        'category'    => $category,
        'imageUrl'    => $imageUrl,
        'ticketUrl'   => $e['url'] ?? '',   // the page where the user buys tickets
    ];
}

// GET /api/events
function listEvents($config)
{
    sendEventList($config, trim($_GET['keyword'] ?? ''));
}

// GET /api/events/search?q=riverdance
function searchEvents($config)
{
    $q = trim($_GET['q'] ?? '');
    if ($q === '') {
        sendError('Missing search text. Use ?q=your+words', 400);
    }
    sendEventList($config, $q);
}

// Builds the question for Ticketmaster, asks it, and sends back the list.
function sendEventList($config, $keyword)
{
    // Page number and page size (we allow between 1 and 50 events per page)
    $size = min(max((int)($_GET['size'] ?? 20), 1), 50);
    $page = max((int)($_GET['page'] ?? 0), 0);

    // These are the settings we send to Ticketmaster. We only want South Africa.
    $params = [
        'countryCode' => 'ZA',
        'size'        => $size,
        'page'        => $page,
        'sort'        => 'date,asc',   // earliest events first
    ];

    if (!empty($_GET['city'])) {
        $params['city'] = $_GET['city'];
    }
    if (!empty($_GET['category'])) {
        $params['classificationName'] = $_GET['category'];   // e.g. Music, Sports
    }
    if ($keyword !== '') {
        $params['keyword'] = $keyword;
    }

    // Dates must look like 2026-10-31
    foreach (['from' => 'startDateTime', 'to' => 'endDateTime'] as $name => $tmName) {
        if (!empty($_GET[$name])) {
            if (!preg_match('/^\d{4}-\d{2}-\d{2}$/', $_GET[$name])) {
                sendError("The '$name' date must look like 2026-10-31", 400);
            }
            $time = ($name === 'from') ? 'T00:00:00Z' : 'T23:59:59Z';
            $params[$tmName] = $_GET[$name] . $time;
        }
    }

    [$status, $data] = callTicketmaster('events.json', $params, $config['ticketmaster_key']);

    if ($status != 200 || $data === null) {
        sendError('Could not get events from Ticketmaster. Please try again.', 502);
    }

    // Make a simple list. If there are no events, "_embedded" is missing.
    $events = [];
    foreach ($data['_embedded']['events'] ?? [] as $e) {
        $events[] = simplifyEvent($e, 200);   // short description for the list
    }

    sendJson([
        'events'        => $events,
        'page'          => $data['page']['number'] ?? 0,
        'size'          => $data['page']['size'] ?? $size,
        'totalPages'    => $data['page']['totalPages'] ?? 0,
        'totalElements' => $data['page']['totalElements'] ?? 0,
    ]);
}

// GET /api/events/{id}
function getEvent($config, $id)
{
    // Event ids only contain letters, numbers, - and _
    if (!preg_match('/^[A-Za-z0-9_-]+$/', $id)) {
        sendError('That event id does not look right.', 400);
    }

    [$status, $data] = callTicketmaster("events/$id.json", [], $config['ticketmaster_key']);

    if ($status == 404) {
        sendError('Event not found.', 404);
    }
    if ($status != 200 || $data === null) {
        sendError('Could not get the event from Ticketmaster. Please try again.', 502);
    }

    sendJson(simplifyEvent($data));   // full description for the detail screen
}