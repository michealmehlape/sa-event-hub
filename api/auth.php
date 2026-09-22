<?php
// The Firebase connection, and our own quick check of the login token.
//
// We check the token ourselves instead of using the slow part of the Firebase
// library, because that part was freezing on this server. We still use the
// library for the Database, since that part works fine and fast.

use Kreait\Firebase\Factory;
use Firebase\JWT\JWT;
use Firebase\JWT\Key;

// Connects to Firebase using our private key. Used only for the Database.
function getFactory($config)
{
    static $factory = null;
    if ($factory === null) {
        $factory = (new Factory)
            ->withServiceAccount($config['firebase_key_path'])
            ->withDatabaseUri($config['database_url']);
    }
    return $factory;
}

function getDatabase($config)
{
    return getFactory($config)->createDatabase();
}

// Turns the strange text inside a login token back into normal text
function base64UrlDecode($text)
{
    $extra = strlen($text) % 4;
    if ($extra) {
        $text .= str_repeat('=', 4 - $extra);
    }
    return base64_decode(strtr($text, '-_', '+/'));
}

// Downloads Google's public keys (needed to check that a token is real).
// We keep a copy for 1 hour, so we do not download it on every single request.
function getGooglePublicKeys()
{
    $cacheFile = sys_get_temp_dir() . '/sa_event_hub_google_keys.json';

    if (file_exists($cacheFile) && (time() - filemtime($cacheFile)) < 3600) {
        return json_decode(file_get_contents($cacheFile), true);
    }

    $url = 'https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com';
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 5);
    curl_setopt($ch, CURLOPT_TIMEOUT, 8);
    $response = curl_exec($ch);
    $status   = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($status === 200 && $response !== false) {
        file_put_contents($cacheFile, $response);
        return json_decode($response, true);
    }

    // If the download failed, use the old copy if we have one
    if (file_exists($cacheFile)) {
        return json_decode(file_get_contents($cacheFile), true);
    }

    sendError('Could not check your login right now. Please try again.', 504);
}

// Checks the login token. If it is good, returns who the user is.
// If it is not good, sends an error and stops.
function requireUser($config)
{
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? '';
    if (!preg_match('/^Bearer\s+(.+)$/i', $header, $matches)) {
        sendError('Please log in first. The login token is missing.', 401);
    }
    $idToken = $matches[1];

    // A token has 3 parts, separated by dots
    $parts = explode('.', $idToken);
    if (count($parts) !== 3) {
        sendError('Your login is not valid. Please log in again.', 401);
    }

    // Read which key was used to sign this token
    $header = json_decode(base64UrlDecode($parts[0]), true);
    $keyId  = $header['kid'] ?? null;

    $certs = getGooglePublicKeys();
    if ($keyId === null || !isset($certs[$keyId])) {
        sendError('Your login is not valid. Please log in again.', 401);
    }

    $publicKey = openssl_pkey_get_public($certs[$keyId]);
    if ($publicKey === false) {
        sendError('Could not check your login right now. Please try again.', 500);
    }

    try {
        $payload = JWT::decode($idToken, new Key($publicKey, 'RS256'));
    } catch (Throwable $e) {
        error_log('Token check failed: ' . $e->getMessage());
        sendError('Your login is not valid or has expired. Please log in again.', 401);
    }

    // Make sure the token was made for OUR app, not some other app
    $projectId = $config['firebase_project_id'];
    $goodAudience = ($payload->aud ?? '') === $projectId;
    $goodIssuer   = ($payload->iss ?? '') === "https://securetoken.google.com/$projectId";

    if (!$goodAudience || !$goodIssuer || empty($payload->sub)) {
        sendError('Your login is not valid. Please log in again.', 401);
    }

    return [
        'uid'   => $payload->sub,
        'email' => $payload->email ?? '',
        'name'  => $payload->name ?? '',
    ];
}