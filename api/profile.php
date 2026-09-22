<?php
// The user's profile. It is kept in Firebase at users/{userId}/profile

// The languages and interests we allow
const ALLOWED_LANGUAGES = ['en', 'zu', 'af'];
const ALLOWED_INTERESTS = ['Music', 'Sports', 'Arts & Culture', 'Comedy', 'Business'];

// Gets the profile. If the user has none yet, it creates a starting one.
function loadProfile($config, $user)
{
    $ref     = getDatabase($config)->getReference('users/' . $user['uid'] . '/profile');
    $profile = $ref->getValue();

    if (!is_array($profile)) {
        // First time: ask Firebase for the user's name and email
                // First time: use the name and email that were inside the login token
        $name  = $user['name'] !== '' ? $user['name'] : $user['email'];
        $email = $user['email'];

        $profile = [
            'name'                 => $name,
            'email'                => $email,
            'language'             => 'en',
            'interests'            => [],
            'points'               => 0,
            'notificationsEnabled' => true,
        ];
        $ref->set($profile);
    }

    // Firebase does not keep empty lists, so make sure the list is there
    $profile['interests'] = array_values($profile['interests'] ?? []);

    return [$ref, $profile];
}

// GET /api/profile
function getProfile($config, $user)
{
    [$ref, $profile] = loadProfile($config, $user);
    sendJson($profile);
}

// PUT /api/profile
// The app can send any of: name, language, interests, notificationsEnabled
function updateProfile($config, $user)
{
    $body = readJsonBody();
    [$ref, $profile] = loadProfile($config, $user);

    $updates = [];

    if (array_key_exists('name', $body)) {
        $name = trim((string)$body['name']);
        if ($name === '' || mb_strlen($name) > 100) {
            sendError('The name must be between 1 and 100 characters.', 400);
        }
        $updates['name'] = $name;
    }

    if (array_key_exists('language', $body)) {
        if (!in_array($body['language'], ALLOWED_LANGUAGES, true)) {
            sendError('The language must be one of: en, zu, af.', 400);
        }
        $updates['language'] = $body['language'];
    }

    if (array_key_exists('interests', $body)) {
        if (!is_array($body['interests'])) {
            sendError('The interests must be a list.', 400);
        }
        foreach ($body['interests'] as $interest) {
            if (!in_array($interest, ALLOWED_INTERESTS, true)) {
                sendError('Unknown interest: ' . json_encode($interest), 400);
            }
        }
        $updates['interests'] = array_values(array_unique($body['interests']));
    }

    if (array_key_exists('notificationsEnabled', $body)) {
        if (!is_bool($body['notificationsEnabled'])) {
            sendError('notificationsEnabled must be true or false.', 400);
        }
        $updates['notificationsEnabled'] = $body['notificationsEnabled'];
    }

    if (count($updates) === 0) {
        sendError('Nothing to update. Send name, language, interests or notificationsEnabled.', 400);
    }

    $ref->update($updates);

    sendJson(array_merge($profile, $updates));
}