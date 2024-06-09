package com.epi.epilog.presentation.theme.api

import com.epi.epilog.presentation.ApiResponse
import com.epi.epilog.presentation.Blood
import com.epi.epilog.presentation.BloodSugarDatas
import com.epi.epilog.presentation.MedicineCheckListDatas
import com.epi.epilog.presentation.UpdateChecklistStatusRequest
import com.epi.epilog.presentation.theme.Data
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("/api/diabetes/bloodsugars")
    fun getBloodSugarDatas(
        @Query("date") date: String,
        @Header("Authorization") authToken: String
    ) : Call<BloodSugarDatas>

    @GET("/api/medicines")
    fun getMedicineChecklist(
        @Query("date") date: String,
        @Header("Authorization") authToken: String
    ) : Call<MedicineCheckListDatas>

    @PATCH("api/medicines/{chklstId}")
    fun updateMedicalChecklist(
        @Path("chklstId") id: Int,
        @Body requestBody: UpdateChecklistStatusRequest,
        @Header("Authorization") authToken: String
    ): Call<ResponseBody>
}


data class SensorData(
    val x: Float,
    val y: Float,
    val z: Float
)
