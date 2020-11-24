package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPostBasedOnIdService {
    @GET("/api/v1/hbtGramPost")
    fun getPostBasedOnId(@Query("_id") postId: String): Call<Any>
}