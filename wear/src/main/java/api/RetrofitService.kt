package com.epi.epilog.presentation.theme.api

import com.epi.epilog.presentation.theme.Data
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Headers

public interface RetrofitService {
    @Headers("Content-Type: application/json")
    @POST("api/auth/login/code")
    fun postData(@Body data: Data): Call<String>
    @POST("api/detection/fall")
    fun postSensorData(@Body data: List<SensorData>, @Header("Authorization") authToken: String): Call<Boolean>

}
data class SensorData(
    val x: Float,
    val y: Float,
    val z: Float
)
