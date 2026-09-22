<?php
// The front door of the API. Every request arrives here first.

// Never show PHP warnings to the app. They would break the JSON.
ini_set('display_errors', '0');
error_reporting(E_ALL);

// The Firebase library lives in the private folder
require '/home/phatudu1u9f9/private/vendor/autoload.php';

require __DIR__ . '/helpers.php';
require __DIR__ . '/auth.php';
require __DIR__ . '/events.php';
require __DIR__ . '/favourites.php';
require __DIR__ . '/profile.php';
require __DIR__ . '/posts.php';

// Load our secret settings from the private folder
$config = require '/home/phatudu1u9f9/private/config.php';

try {
    // Work out what the app asked for. Example: GET /api/events/search
    $method = $_SERVER['REQUEST_METHOD'];
    $path   = trim(parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH), '/');
    $parts  = explode('/', $path);          // ["api", "events", "search"]

    // The home page, only used to check that the API is running
    if ($path === '') {
        sendJson(['name' => 'SA Event Hub API', 'status' => 'running']);
    }

    if ($parts[0] === 'api') {
        $resource = $parts[1] ?? '';
        $id       = $parts[2] ?? null;

        // ----- EVENTS (no login needed) -----
        if ($resource === 'events' && $method === 'GET') {
            if ($id === null) {
                listEvents($config);              // GET /api/events
            } elseif ($id === 'search') {
                searchEvents($config);            // GET /api/events/search
            } else {
                getEvent($config, $id);           // GET /api/events/{id}
            }
        }

        // ----- FAVOURITES (login needed) -----
        if ($resource === 'favourites') {
            $user = requireUser($config);         // stops with 401 if not logged in

            if ($method === 'GET' && $id === null) {
                listFavourites($config, $user);           // GET /api/favourites
            }
            if ($method === 'POST' && $id === null) {
                addFavourite($config, $user);             // POST /api/favourites
            }
            if ($method === 'DELETE' && $id !== null) {
                removeFavourite($config, $user, $id);     // DELETE /api/favourites/{id}
            }
            sendError('That method is not allowed here.', 405);
        }

        // ----- PROFILE (login needed) -----
        if ($resource === 'profile') {
            $user = requireUser($config);

            if ($method === 'GET') {
                getProfile($config, $user);               // GET /api/profile
            }
            if ($method === 'PUT') {
                updateProfile($config, $user);            // PUT /api/profile
            }
            sendError('That method is not allowed here.', 405);
        }
                // ----- COMMUNITY (login needed) -----
        if ($resource === 'community') {
            $user = requireUser($config);
            $sub  = $parts[2] ?? '';   // "posts"

            if ($sub === 'posts') {
                $postId    = $parts[3] ?? null;
                $subAction = $parts[4] ?? null;   // "comments"

                if ($postId === null) {
                    if ($method === 'GET') listPosts($config);
                    if ($method === 'POST') createPost($config, $user);
                } elseif ($subAction === 'comments') {
                    if ($method === 'GET') listComments($config, $postId);
                    if ($method === 'POST') addComment($config, $user, $postId);
                }
            }
            sendError('That method is not allowed here.', 405);
        }
    }

    sendError('Route not found.', 404);

} catch (Throwable $e) {
    // Something unexpected went wrong. Save the details in the server log,
    // but only tell the app something short.
    error_log('API error: ' . $e->getMessage());
    sendError('Something went wrong on the server.', 500);
}