//package com.epi.epilog.presentation.theme.api
//
//import com.epi.epilog.presentation.ApiResponse
//import com.epi.epilog.presentation.Blood
//import com.epi.epilog.presentation.BloodSugarDatas
//import com.epi.epilog.presentation.MedicineCheckListDatas
//import com.epi.epilog.presentation.UpdateChecklistStatusRequest
//import com.epi.epilog.presentation.theme.Data
//import okhttp3.ResponseBody
//import retrofit2.Call
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.Header
//import retrofit2.http.Headers
//import retrofit2.http.PATCH
//import retrofit2.http.POST
//import retrofit2.http.Path
//import retrofit2.http.Query
//
//
//interface RetrofitService {
//
//    @Headers("Content-Type: application/json")
//    @POST("api/auth/login/code")
//    fun postData(@Body data: Data): Call<String>
//
//
//
//
//
//}
//
//data class TokenData(val token: String)
//
//data class SensorData(
//    val x: Float,
//    val y: Float,
//    val z: Float
//)
//
//data class MealUpdateInfo(
//    val time: String,
//    val status: String
//)
//
//data class MealCheckListResponse(
//    val date: String,
//    val checklist: List<MealCheckItem>
//)
//
//data class MealCheckItem(
//    val id: Int,
//    val goalTime: String,
//    val title: String,
//    var isComplete: Boolean,
//    var state: State
//)
//
//enum class State {
//    식사함, 건너뜀, 상태없음
//}
//
//data class LocationData(
//    val latitude: Double,
//    val longitude: Double
//)
