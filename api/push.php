<?php
// Push notifications through Firebase Cloud Messaging (FCM).

use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Exception\Messaging\NotFound;

// Sends the JSON answer to the app first, then runs $afterwards.
// This way a slow notification can never make the app wait.
function respondThenRun($data, $status, $afterwards)
{
    ignore_user_abort(true);
    set_time_limit(60);

    $json = json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
    http_response_code($status);
    header('Content-Type: application/json; charset=utf-8');
    header('Content-Length: ' . strlen($json));
    header('Connection: close');
    echo $json;

    if (function_exists('fastcgi_finish_request')) {
        fastcgi_finish_request();
    } else {
        while (ob_get_level() > 0) {
            ob_end_flush();
        }
        flush();
    }

    $afterwards();
    exit;
}

// Tells the author of a post that someone commented on it.
// A problem here is only written to the log. The comment has already been saved.
function notifyPostAuthor($config, $post, $postId, $commenter, $commenterName)
{
    try {
        $authorId = $post['userId'] ?? null;
        if ($authorId === null || $authorId === $commenter['uid']) {
            return;   // nobody to tell, or the person commented on their own post
        }

        $profileRef = getDatabase($config)->getReference('users/' . $authorId . '/profile');
        $profile    = $profileRef->getValue();

        if (!is_array($profile) || empty($profile['fcmToken'])) {
            return;   // the author has no phone registered
        }
        if (($profile['notificationsEnabled'] ?? true) === false) {
            return;   // the author switched notifications off
        }

        // A "data" message: the app builds the text itself, in the user's language
        $message = CloudMessage::withTarget('token', $profile['fcmToken'])
            ->withData([
                'type'          => 'comment',
                'postId'        => (string)$postId,
                'postAuthor'    => (string)($post['authorName'] ?? ''),
                'postBody'      => mb_substr((string)($post['body'] ?? ''), 0, 200),
                'commenterName' => (string)$commenterName,
            ])
            ->withAndroidConfig(['priority' => 'high']);

        getFactory($config)->createMessaging()->send($message);
        error_log('Push sent to the author of post ' . $postId);

    } catch (NotFound $e) {
        // The phone no longer uses this token (for example the user logged out). Remove it.
        error_log('Push token no longer valid, removing it');
        getDatabase($config)->getReference('users/' . $authorId . '/profile/fcmToken')->remove();
    } catch (Throwable $e) {
        error_log('Push failed: ' . $e->getMessage());
    }
}
