package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetFollowingService {
    @GET("/api/v1/hbtGramFollow")
    fun getFollowings(@Query("follower") userId: String): Call<Any>
}