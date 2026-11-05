<?php
// Push notifications through Firebase Cloud Messaging (FCM).

use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Exception\Messaging\NotFound;
use Kreait\Firebase\Messaging\AndroidConfig;

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
    $authorId = null;
    try {
        error_log('PUSH: started for post ' . $postId);

        $authorId = $post['userId'] ?? null;
        if ($authorId === null) {
            error_log('PUSH: stopped, the post has no userId');
            return;
        }
        if ($authorId === $commenter['uid']) {
            error_log('PUSH: stopped, the commenter is the post author');
            return;
        }

        $profile = getDatabase($config)->getReference('users/' . $authorId . '/profile')->getValue();
        if (!is_array($profile)) {
            error_log('PUSH: stopped, the author has no profile');
            return;
        }
        if (empty($profile['fcmToken'])) {
            error_log('PUSH: stopped, the author has no fcmToken');
            return;
        }
        if (($profile['notificationsEnabled'] ?? true) === false) {
            error_log('PUSH: stopped, the author switched notifications off');
            return;
        }

        error_log('PUSH: sending to ' . substr($profile['fcmToken'], 0, 12) . '...');

                $message = CloudMessage::new()
                    ->withToken($profile['fcmToken'])
                    ->withData([
                        'type'          => 'comment',
                        'postId'        => (string)$postId,
                        'postAuthor'    => (string)($post['authorName'] ?? ''),
                        'postBody'      => mb_substr((string)($post['body'] ?? ''), 0, 200),
                        'commenterName' => (string)$commenterName,
                    ])
                    ->withAndroidConfig(AndroidConfig::fromArray(['priority' => 'high']));

        getFactory($config)->createMessaging()->send($message);
        error_log('PUSH: sent to the author of post ' . $postId);

    } catch (NotFound $e) {
        error_log('PUSH: token no longer valid, removing it');
        if ($authorId !== null) {
            getDatabase($config)->getReference('users/' . $authorId . '/profile/fcmToken')->remove();
        }
    } catch (Throwable $e) {
        error_log('PUSH: failed: ' . $e->getMessage());
    }
}
