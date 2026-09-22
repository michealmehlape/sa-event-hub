<?php
// Small helper functions that every part of the API uses.

// Sends JSON back to the app and stops the script.
function sendJson($data, $status = 200)
{
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    exit;
}

// Sends an error message as JSON, for example: {"error": "Event not found"}
function sendError($message, $status)
{
    sendJson(['error' => $message], $status);
}

// Asks the Ticketmaster API for data.
// Returns two things: the HTTP status code and the data (as a PHP array).
function callTicketmaster($path, $params, $apiKey)
{
    $params['apikey'] = $apiKey;
    $url = 'https://app.ticketmaster.com/discovery/v2/' . $path . '?' . http_build_query($params);

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 20);
    $response = curl_exec($ch);
    $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    // (No need to close the curl handle in PHP 8, it closes by itself.)

    if ($response === false) {
        return [0, null];   // we could not reach Ticketmaster at all
    }
    return [$status, json_decode($response, true)];
}
// Reads the JSON that the app sent with a POST or PUT request.
function readJsonBody()
{
    $text = file_get_contents('php://input');
    $data = json_decode($text, true);
    if (!is_array($data)) {
        sendError('The request body must be valid JSON.', 400);
    }
    return $data;
}