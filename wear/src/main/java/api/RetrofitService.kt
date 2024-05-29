package com.epi.epilog.presentation.theme.api

import com.epi.epilog.presentation.ApiResponse
import com.epi.epilog.presentation.Blood
import com.epi.epilog.presentation.theme.Data
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

public interface RetrofitService {


    @Headers("Content-Type: application/json")
    @POST("api/auth/login/code")
    fun postData(@Body data: Data): Call<String>

    @POST("api/detection/fall")
    fun postSensorData(@Body data: List<SensorData>, @Header("Authorization") authToken: String): Call<Boolean>

    @POST("api/diabetes/bloodsugar")
    fun postBloodSugarData(
        @Body data: Blood,
        @Header("Authorization") authToken: String
    ): Call<ApiResponse>

//    companion object {
//        private const val BASE_URL = "http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/"
//
//        fun create(): RetrofitService {
//            val retrofit = Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build()
//            return retrofit.create(RetrofitService::class.java)
//        }
//    }

}

data class SensorData(
    val x: Float,
    val y: Float,
    val z: Float
)
