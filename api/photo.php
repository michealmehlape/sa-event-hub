<?php
// Profile pictures.
// The picture  is kept in Supabase Storage (blob storage).
// Only its web address is saved in the Firebase database, under users/{userId}/profile/photoUrl.
// The Supabase secret key stays on this server. It is never put in the app.

const MAX_PHOTO_BYTES = 2 * 1024 * 1024;   // 2 MB

// POST /api/profile/photo   (a multipart form with one file called "photo")
function uploadProfilePhoto($config, $user)
{
    if (!isset($_FILES['photo']) || $_FILES['photo']['error'] !== UPLOAD_ERR_OK) {
        sendError('Please send a picture in the "photo" field.', 400);
    }
    $file = $_FILES['photo'];

    if ($file['size'] > MAX_PHOTO_BYTES) {
        sendError('The picture must be smaller than 2 MB.', 413);
    }

    // Check what the file really is. We do not trust the name the app sent.
    $allowed = ['image/jpeg' => 'jpg', 'image/png' => 'png', 'image/webp' => 'webp'];
    $info    = @getimagesize($file['tmp_name']);
    $mime    = $info['mime'] ?? '';
    if (!isset($allowed[$mime])) {
        sendError('Only JPEG, PNG or WebP pictures are allowed.', 415);
    }

    $bytes = file_get_contents($file['tmp_name']);
    $path  = $user['uid'] . '/avatar.' . $allowed[$mime];

    $publicUrl = supabaseUpload($config, $path, $bytes, $mime);

    // "?v=" makes the app load the new picture instead of an old cached one
    $photoUrl = $publicUrl . '?v=' . time();

    getDatabase($config)
        ->getReference('users/' . $user['uid'] . '/profile/photoUrl')
        ->set($photoUrl);

    sendJson(['photoUrl' => $photoUrl], 201);
}

// Sends the file to Supabase Storage and returns its public web address.
function supabaseUpload($config, $path, $bytes, $mime)
{
    $base   = rtrim($config['supabase_url'], '/');
    $bucket = $config['supabase_bucket'];
    $key    = $config['supabase_key'];

    $headers = [
        'apikey: ' . $key,
        'Content-Type: ' . $mime,
        'x-upsert: true',          // replace the old picture if there is one
    ];
    // Old-style keys are JWTs and also need an Authorization header.
    // New keys (they start with "sb_") only need the apikey header.
    if (strpos($key, 'sb_') !== 0) {
        $headers[] = 'Authorization: Bearer ' . $key;
    }

    $ch = curl_init($base . '/storage/v1/object/' . $bucket . '/' . $path);
    curl_setopt_array($ch, [
        CURLOPT_POST           => true,
        CURLOPT_POSTFIELDS     => $bytes,
        CURLOPT_HTTPHEADER     => $headers,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT        => 20,
    ]);
    $response = curl_exec($ch);
    $status   = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    if ($response === false || $status < 200 || $status >= 300) {
        error_log('Supabase upload failed: HTTP ' . $status . ' ' . $response);
        sendError('Could not store the picture. Please try again.', 502);
    }

    return $base . '/storage/v1/object/public/' . $bucket . '/' . $path;
}
