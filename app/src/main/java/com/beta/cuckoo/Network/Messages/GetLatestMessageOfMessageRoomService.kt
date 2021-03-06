package com.beta.cuckoo.Network.Messages

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetLatestMessageOfMessageRoomService {
    @GET("/api/v1/message/getLatestMessageOfMessageRoom")
    fun getLatestMessageOfMessageRoom(@Query("chatRoomId") chatRoomId: String): Call<Any>
}