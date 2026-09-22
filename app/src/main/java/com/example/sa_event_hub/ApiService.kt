package com.example.sa_event_hub

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface ApiService {

    // GET /api/events
    @GET("api/events")
    fun getEvents(
        @Query("city") city: String?,
        @Query("category") category: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<EventListResponse>

    // GET /api/favourites
    @GET("api/favourites")
    fun getFavourites(): Call<FavouriteListResponse>

    // POST /api/favourites
    @POST("api/favourites")
    fun addFavourite(@Body body: FavouriteRequest): Call<Event>

    // DELETE /api/favourites/{id}
    @DELETE("api/favourites/{id}")
    fun removeFavourite(@Path("id") id: String): Call<MessageResponse>

    // GET /api/events/search?q=...
    @GET("api/events/search")
    fun searchEvents(
        @Query("q") query: String,
        @Query("city") city: String?,
        @Query("category") category: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<EventListResponse>

    // GET /api/events/{id}
    @GET("api/events/{id}")
    fun getEvent(@Path("id") id: String): Call<Event>

    // GET /api/community/posts
    @GET("api/community/posts")
    fun getPosts(): Call<PostListResponse>

    // POST /api/community/posts
    @POST("api/community/posts")
    fun createPost(@Body body: PostRequest): Call<Post>

    // GET /api/community/posts/{postId}/comments
    @GET("api/community/posts/{postId}/comments")
    fun getComments(@Path("postId") postId: String): Call<CommentListResponse>

    // POST /api/community/posts/{postId}/comments
    @POST("api/community/posts/{postId}/comments")
    fun addComment(@Path("postId") postId: String, @Body body: CommentRequest): Call<Comment>


}
