package com.beta.myhbt_api.Controller.LikesAndComments

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetAllPostLikesService {
    @GET("/api/v1/cuckooPostLike")
    fun getPostLikes(@Query("postId") postId: String): Call<Any>
}