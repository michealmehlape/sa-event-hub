<?php
// Handles community posts and comments.

// GET /api/community/posts
function listPosts($config)
{
    $ref = getDatabase($config)->getReference('posts');
    $raw = $ref->getValue();

    $posts = [];
    if (is_array($raw)) {
        foreach ($raw as $postId => $post) {
            $post['id'] = $postId;
            $posts[] = $post;
        }
        // Newest first.
        usort($posts, fn($a, $b) => ($b['createdAt'] ?? 0) <=> ($a['createdAt'] ?? 0));
    }

    sendJson(['posts' => $posts]);
}

// POST /api/community/posts   body: {"body": "..."}
function createPost($config, $user)
{
    $data = readJsonBody();
    $body = trim($data['body'] ?? '');

    if ($body === '' || mb_strlen($body) > 500) {
        sendError('A post must be between 1 and 500 characters.', 400);
    }

    $authorName = $user['name'] !== '' ? $user['name'] : $user['email'];

    $post = [
        'userId'     => $user['uid'],
        'authorName' => $authorName,
        'body'       => $body,
        'createdAt'  => time(),
    ];

    $newRef = getDatabase($config)->getReference('posts')->push($post);
    $post['id'] = $newRef->getKey();

    sendJson($post, 201);
}

// GET /api/community/posts/{postId}/comments
function listComments($config, $postId)
{
    checkPostId($postId);

    $raw = getDatabase($config)->getReference('comments/' . $postId)->getValue();

    $comments = [];
    if (is_array($raw)) {
        foreach ($raw as $commentId => $comment) {
            $comment['id'] = $commentId;
            $comments[] = $comment;
        }
        usort($comments, fn($a, $b) => ($a['createdAt'] ?? 0) <=> ($b['createdAt'] ?? 0));
    }

    sendJson(['comments' => $comments]);
}

// POST /api/community/posts/{postId}/comments   body: {"body": "..."}
function addComment($config, $user, $postId)
{
    checkPostId($postId);

    // The post must exist before a comment can be added to it.
    $postRef = getDatabase($config)->getReference('posts/' . $postId);
    if (!$postRef->getSnapshot()->exists()) {
        sendError('That post does not exist.', 404);
    }

    $data = readJsonBody();
    $body = trim($data['body'] ?? '');

    if ($body === '' || mb_strlen($body) > 300) {
        sendError('A comment must be between 1 and 300 characters.', 400);
    }

    $authorName = $user['name'] !== '' ? $user['name'] : $user['email'];

    $comment = [
        'userId'     => $user['uid'],
        'authorName' => $authorName,
        'body'       => $body,
        'createdAt'  => time(),
    ];

    $newRef = getDatabase($config)->getReference('comments/' . $postId)->push($comment);
    $comment['id'] = $newRef->getKey();

    sendJson($comment, 201);
}

// A Firebase push key looks like -NxAbCdEfGh12345.
function checkPostId($postId)
{
    if (!preg_match('/^[A-Za-z0-9_-]+$/', $postId)) {
        sendError('That post id does not look right.', 400);
    }
}