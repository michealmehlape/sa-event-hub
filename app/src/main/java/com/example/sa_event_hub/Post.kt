package com.example.sa_event_hub

// A single community post.
data class Post(
    val id: String,
    val userId: String,
    val authorName: String,
    val body: String,
    val createdAt: Long
)

// A single comment on a post.
data class Comment(
    val id: String,
    val userId: String,
    val authorName: String,
    val body: String,
    val createdAt: Long
)

data class PostListResponse(val posts: List<Post>)
data class CommentListResponse(val comments: List<Comment>)
data class PostRequest(val body: String)
data class CommentRequest(val body: String)