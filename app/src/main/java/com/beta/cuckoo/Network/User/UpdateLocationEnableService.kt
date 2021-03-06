package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PATCH
import retrofit2.http.Query

interface UpdateLocationEnableService {
    @PATCH("/api/v1/users/updateMe")
    @FormUrlEncoded
    fun updateLocationEnable(@Field("locationEnabled") locationEnabled: String, @Query("userId") userId: String): Call<Any>
}