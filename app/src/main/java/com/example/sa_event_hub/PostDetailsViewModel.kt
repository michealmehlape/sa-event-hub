package com.example.sa_event_hub

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Loads and adds comments for a single post.
class PostDetailsViewModel : ViewModel() {

    val comments = MutableLiveData<List<Comment>>(emptyList())
    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)

    fun loadComments(postId: String) {
        isLoading.value = true
        errorMessage.value = null

        ApiClient.service.getComments(postId).enqueue(object : Callback<CommentListResponse> {
            override fun onResponse(call: Call<CommentListResponse>, response: Response<CommentListResponse>) {
                isLoading.value = false
                val list = response.body()?.comments

                if (response.isSuccessful && list != null) {
                    comments.value = list
                    errorMessage.value = if (list.isEmpty()) "No comments yet." else null
                } else {
                    errorMessage.value = "Could not load comments (error ${response.code()})."
                }
            }

            override fun onFailure(call: Call<CommentListResponse>, t: Throwable) {
                isLoading.value = false
                Log.w("PostDetailsViewModel", "Could not reach the API", t)
                errorMessage.value = "No internet connection."
            }
        })
    }

    fun addComment(postId: String, body: String) {
        val text = body.trim()
        if (text.isEmpty()) return

        ApiClient.service.addComment(postId, CommentRequest(text)).enqueue(object : Callback<Comment> {
            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                if (response.isSuccessful) {
                    loadComments(postId)
                } else {
                    errorMessage.value = "Could not add the comment (error ${response.code()})."
                }
            }

            override fun onFailure(call: Call<Comment>, t: Throwable) {
                errorMessage.value = "No internet connection."
            }
        })
    }
}