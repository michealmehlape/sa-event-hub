package com.example.sa_event_hub

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Loads the community post list and lets a new post be created.
class CommunityViewModel : ViewModel() {

    val posts = MutableLiveData<List<Post>>(emptyList())
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)
    val postCreated = MutableLiveData(false)

    fun loadPosts() {
        isLoading.value = true
        errorMessage.value = null

        ApiClient.service.getPosts().enqueue(object : Callback<PostListResponse> {
            override fun onResponse(call: Call<PostListResponse>, response: Response<PostListResponse>) {
                isLoading.value = false
                val list = response.body()?.posts

                if (response.isSuccessful && list != null) {
                    posts.value = list
                    errorMessage.value = if (list.isEmpty()) "No posts yet. Be the first to share something." else null
                } else {
                    errorMessage.value = "Could not load posts (error ${response.code()})."
                }
            }

            override fun onFailure(call: Call<PostListResponse>, t: Throwable) {
                isLoading.value = false
                Log.w("CommunityViewModel", "Could not reach the API", t)
                errorMessage.value = "No internet connection."
            }
        })
    }

    fun createPost(body: String) {
        val text = body.trim()
        if (text.isEmpty()) return

        ApiClient.service.createPost(PostRequest(text)).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    postCreated.value = true
                    loadPosts()
                } else {
                    errorMessage.value = "Could not create the post (error ${response.code()})."
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                errorMessage.value = "No internet connection."
            }
        })
    }
}