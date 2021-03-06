package com.beta.cuckoo.Network.Messages

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetMessageRoomOfUserService {
    @GET("/api/v1/messageRoom/getMessageRoomOfUser")
    fun getMessageRoomOfUserService(@Query("userId") userId: String): Call<Any>
}