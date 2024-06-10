package com.epi.epilog.presentation.theme.api

import com.epi.epilog.presentation.ApiResponse
import com.epi.epilog.presentation.Blood
import com.epi.epilog.presentation.BloodSugarDatas
import com.epi.epilog.presentation.MedicineCheckListDatas
import com.epi.epilog.presentation.UpdateChecklistStatusRequest
import com.epi.epilog.presentation.theme.Data
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {

    @Headers("Content-Type: application/json")
    @POST("api/auth/login/code")
    fun postData(@Body data: Data): Call<String>

    @POST("api/detection/fall")
    fun postSensorData(
        @Body data: List<SensorData>,
        @Header("Authorization") authToken: String
    ): Call<Boolean>

    @POST("api/diabetes/bloodsugar")
    fun postBloodSugarData(
        @Body data: Blood,
        @Header("Authorization") authToken: String
    ): Call<ApiResponse>

    @GET("api/diabetes/bloodsugars")
    fun getBloodSugarDatas(
        @Query("date") date: String,
        @Header("Authorization") authToken: String
    ): Call<BloodSugarDatas>

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


    @POST("api/detection/emergency")
    fun postLocationData(
        @Body locationData: LocationData,
        @Header("Authorization") authToken: String
    ): Call<ApiResponse>

    @Headers("Content-Type: application/json")
    @GET("api/meals")
    fun getMealCheckList(
        @Query("date") date: String,
        @Header("Authorization") authToken: String
    ): Call<MealCheckListResponse>

    @PATCH("api/meals/{chklistId}")
    fun updateMealStatus(
        @Path("chklistId") chklistId: Int,
        @Header("Authorization") authToken: String,
        @Body updateInfo: MealUpdateInfo
    ): Call<ApiResponse>
}

data class SensorData(
    val x: Float,
    val y: Float,
    val z: Float
)

data class MealUpdateInfo(
    val time: String,
    val status: String
)

data class MealCheckListResponse(
    val date: String,
    val checklist: List<MealCheckItem>
)

data class MealCheckItem(
    val id: Int,
    val goalTime: String,
    val title: String,
    var isComplete: Boolean,
    var state: State // Use enum type here
)

enum class State {
    식사함, 건너뜀, 상태없음 // Adjust these based on your actual data
}

data class LocationData(
    val latitude: Double,
    val longitude: Double
)
