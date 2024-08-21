package com.epi.epilog.api

import android.os.Parcel
import android.os.Parcelable
import com.epi.epilog.ApiResponse
import com.epi.epilog.medicine.MedicineBottomSheetFragment
import okhttp3.ResponseBody

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.Streaming
import retrofit2.http.Path

object RetrofitClient {
    private const val BASE_URL = "http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val retrofitService: RetrofitService by lazy {
        retrofit.create(RetrofitService::class.java)
    }
}

interface RetrofitService {
    @Headers("Content-Type: application/json")
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<String>

    @Headers("Content-Type: application/json")
    @POST("/api/auth/signup")
    fun signUp(@Body request: SignUpRequest): Call<SignUpResponse>

    @Headers("Content-Type: application/json")
    @GET("/api/auth/validation")
    fun validateId(@Query("id") userId: String): Call<ApiResponse>

    @Headers("Content-Type: application/json")
    @GET("/api/medicines")
    fun getMedicationChecklist(@Query("date") date: String, @Header("Authorization") token: String): Call<MedicationChecklistResponse>

    @POST("/api/fcm/token")
    fun postToken(
        @Header("Authorization") authToken:  String,
        @Body tokenData: TokenData
    ): Call<ApiResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/medications")
    fun addMedication(
        @Header("Authorization") token: String,
        @Body request: MedicationRequest
    ): Call<ApiResponse>

    @Headers("Content-Type: application/json")
    @GET("/auth/test")
    fun testApi(@Header("Authorization") authToken: String): Call<Void>

    //일지 등록 API
    @POST("api/logs")
    fun addDiary(
        @Header("Authorization") token: String,
        @Body request: DiaryRequest
    ) : Call<ApiResponse>

    //일지 상세 조회 API
    @GET("/api/logs/{logId}")
    fun getDiaryDetail(
        @Path("logId") logId: Int,
        @Header("Authorization") token: String,
    ) : Call <DiaryDetailResponse>

    //메인 캘린더 월(일)별 일지 개수 조회 API
    @Headers("Content-Type: application/json")
    @GET("api/logs/count")
    fun getDiaryCount(
        @Query("date") date: String,
        @Header("Authorization") token: String
    ) : Call<DiaryCountResponse>

    //메인 캘린더 일자별 일지 icon 조회 API
    @Headers("Content-Type: application/json")
    @GET("api/logs")
    fun getDiaryIcon(
        @Query("date") date: String,
        @Header("Authorization") token: String
    ) : Call<DiaryIconResponse>

    //메인 그래프 2 정보 얻어오기
    @Headers("Content-Type: application/json")
    @GET("api/logs/weight")
    fun getGraphWeightBMI(
        @Query("date") date: String,
        @Header("Authorization") token: String
    ): Call<GraphWeightBMIResponse>

    //메인 그래프 1 정보 얻어오기
    @Headers("Content-Type: application/json")
    @GET("api/logs/bloodsugar")
    fun getGraphBloodSugar(
        @Query("date") date: String,
        @Header("Authorization") token: String
    ) : Call<GraphBloodSugarResponse>

    //메인 그래프 TV 정보 얻어오기
    @Headers("Content-Type: application/json")
    @GET("api/logs/bloodsugar/average")
    fun getBloodSugarAverage(
        @Query("date") date: String,
        @Header("Authorization") token: String
    ):Call<GraphBloodSugarAverageResponse>

    //PDF
    @Streaming
    @GET("/api/logs/convert")
    fun downloadPDF(
        @Query("start") startDate: String,
        @Query("end") endDate: String,
        @Header("Authorization") authToken: String,
        @Header("Content-Type") contentType: String = "application/pdf"
    ): Call<ResponseBody>

    //복약체크리스트 상세 조회
    @Headers("Content-Type: application/json")
    @GET("/api/medications/{mcld}")
    fun getMedicationDetail(
        @Path("mcld") medicationId: Int,
        @Header("Authorization") token: String
    ): Call<MedicationDetailResponse>

    //복약체크리스트 약 삭제
    @DELETE("/api/medications/{medicationId}")
    fun deleteMedication(
        @Header("Authorization") token: String,
        @Path("medicationId") medicationId: Int
    ): Call<Void>

    //복약체크리스트 약 수정
    @PATCH("api/medicines/{chklistId}")
    fun updateMedicineStatus(
        @Path("chklistId") chklistId: Int,
        @Header("Authorization") token: String,
        @Body request: MedicationStatusUpdateRequest
    ): Call<ApiResponse>
}

enum class State {
    복용, 미복용, 상태없음
}

//일지 상세 조회 data class
data class DiaryDetailResponse(
    val title: String,
    val keyword: List<String>,
    val fall: Fall?,
    val bloodSugar: String?,
    val systolicBloodPressure: String?,
    val diastolicBloodPressure: String?,
    val heartRate: String?,
    val weight: Float?,
    val bodyFatPercentage: Float?,
    val bodyPhoto: String?, //TODO : 나중에 객체로 수정 필요
    val exercise: Exercise?,
    val mood: Mood?
)
data class Fall(
    val address: String?,
    val mapImage: String?  // mapImage URL을 포함
)

data class Exercise(
    val type: String?,
    val details: String?,
    val keyword: List<String>?
)

data class Mood(
    val type: String?,
    val details: String?,
    val keyword: List<String>?
)


//일자별 icon 조회 data class
data class DiaryIconResponse(
    val date: String?,
    val count: Int,
    val logs: List<titleKeyWordEntry>
)
//일자별 시간 - 키워드 Entry
data class titleKeyWordEntry(
    val id: Int,
    val title: String,
    val keyword: List<String>
)

//월(일)별 일지 조회 data class
data class DiaryCountResponse(
    val year: Int,
    val month: Int,
    val day: List<dayEntry>
)

//일별 일지 개수 조회를 위한Entry
data class dayEntry(
    val date: String?,
    val count: Int
)
//일지 등록 data class
data class DiaryRequest(
    val date: String?,
    val occurenceType: String?,
    val bloodSugar: String?,
    val systolicBloodPressure: String?,
    val diastolicBloodPressure: String?,
    val heartRate: String?,
    val weight: String?,
    val bodyFatPercentage: String?,
    val bodyPhoto: String?,
    val exercise: List<ExerciseEntry>?,
    val mood: List<MoodEntry>?
)

//일지 등록 , 운동 entry
data class ExerciseEntry(
    val type: String?,
    val details: String?
)

//일지 등록 , 기분 entry
data class MoodEntry(
    val type: String?,
    val details: String?
)


data class TokenData(val token: String)

data class GraphBloodSugarAverageResponse(
    val date : String,
    @SerializedName("average") val average: Float,
    @SerializedName("preAverage") val preAverage: Float,
    @SerializedName("postAverage") val postAverage: Float
)

data class GraphBloodSugarResponse(
    val date : String,
    val count : Int,
    val bloodSugars: List<GraphBloodSugars>
)

data class GraphBloodSugars(
    val title: String,
    val bloodSugar: Float
)

data class GraphWeightBMIResponse(
    val year: Int,
    val month: Int,
    val dayWeight: List<GraphWeightBMIDate>,
    val dayBodyFatPercentage: List<GraphWeightBMIDate>
)

data class GraphWeightBMIDate(
    val date: String,
    val value: Float
)


data class MedicationChecklistResponse(
    val date: String,
    val medicationId: Int,
    val checklist: List<ChecklistItem>
)

data class ChecklistItem(
    val id: Int,
    val goalTime: String,
    val title: String,
    val time: String,
    val medicationName: String,
    var isComplete: Boolean,
    var state: State
)
data class LoginRequest(
    val loginId: String,
    val password: String
)

data class SignUpResponse(
    val success: Boolean,
    val code: String,
    val token: String
)

data class SignUpRequest(
    val loginId: String,
    val password: String,
    val name: String,
    val stature: Float,
    val weight: Float,
    val gender: String,
    val protectorName: String,
    val protectorPhone: String,
    val medication: List<Medication>
)

data class MedicationRequest(
    val medicationName: String,
    val times: List<String>,
    val startDate: String,
    val endDate: String?,
    val endless: Boolean,
    val isAlarm: Boolean,
    val weeks: List<String>?,
    val effectiveness: String,
    val precautions: String,
    val storageMethod: String
)

//복약체크리스트 약 상세 조회
data class MedicationDetailResponse(
    val id: Int,
    val nextId: Int?,
    val prevId: Int?,
    val medicationName: String,
    val times: List<String>,
    val isAlarm: Boolean,
    val startDate: String,
    val endDate: String,
    val weeks: List<String>,
    val effectiveness: String,
    val precautions: String,
    val storageMethod: String
)


data class Medication(
    val name: String,
    val times: MutableList<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        mutableListOf<String>().apply {
            parcel.readStringList(this)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeStringList(times)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Medication> {
        override fun createFromParcel(parcel: Parcel): Medication {
            return Medication(parcel)
        }

        override fun newArray(size: Int): Array<Medication?> {
            return arrayOfNulls(size)
        }
    }
}